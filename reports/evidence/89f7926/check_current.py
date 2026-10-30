"""Regression and bounded characterization of 89f7926 against local development server; cleans its own accounts."""
import socket,struct,secrets,subprocess,time,select,math
prefix='rpt_'+secrets.token_hex(4);users=[];clients=[]
def st(s):
 b=s.encode();return struct.pack('>i',len(b))+b
def read(s,n):
 b=b''
 while len(b)<n:
  q=s.recv(n-len(b))
  if not q:raise EOFError()
  b+=q
 return b
def num(s,f):return struct.unpack('>'+f,read(s,struct.calcsize('>'+f)))[0]
def string(s):return read(s,num(s,'i')).decode()
def send(s,n,b=b''):s.sendall(bytes([n])+b)
def packet(s):
 n=num(s,'B')
 if n in (4,45):return n,num(s,'?'),string(s)
 if n==2:
  ok=num(s,'?');name=string(s)
  if ok:read(s,20);string(s)
  return n,ok,name
 if n==6:
  name=string(s);read(s,8);string(s);return n,name
 if n==9:return n,num(s,'i'),num(s,'?'),num(s,'q')
 if n==43:return n,string(s)
 if n==47:return (n,)+struct.unpack('>?4d',read(s,33))
 if n==11:return (n,)+struct.unpack('>6d',read(s,48))
 if n==12:return n,num(s,'?'),num(s,'?'),string(s)
 if n==14:return n,num(s,'?'),num(s,'i')
 if n==18:return n,num(s,'q')
 if n==23:return n,num(s,'?')
 if n==41:return n,string(s),string(s)
 raise AssertionError(('unexpected packet',n))
def expect(s,n):
 p=packet(s);assert p[0]==n,p;return p
def quiet(s,sec=.2):return not select.select([s],[],[],sec)[0]
def login(i,email=None):
 name=prefix+'_'+str(i);users.append(name)
 s=socket.create_connection(('127.0.0.1',12355),timeout=5);clients.append(s)
 send(s,3,st(email or name+'@example.com')+st(name)+st('ReportCurrent2026'));assert expect(s,4)[1]
 send(s,1,st((email or name).upper())+st('ReportCurrent2026'));assert expect(s,2)[1]
 return s,name
def challenge(a,bname):send(a,42,st(bname))
def response(b,aname,accept):send(b,44,st(aname)+struct.pack('>?',accept))
def cleanup():
 for s in clients:
  try:s.close()
  except:pass
 time.sleep(.5)
 quoted=','.join("'"+x+"'" for x in users)
 if quoted:
  ids='SELECT id FROM players WHERE username IN ('+quoted+')'
  sql='BEGIN;DELETE FROM messages WHERE sender_id IN ('+ids+') OR receiver_id IN ('+ids+');DELETE FROM friendships WHERE user_id IN ('+ids+') OR friend_id IN ('+ids+');DELETE FROM match_history WHERE winner_id IN ('+ids+') OR loser_id IN ('+ids+');DELETE FROM players WHERE username IN ('+quoted+');COMMIT;'
  subprocess.run(['/workspace/ltm-env/sysroot/usr/lib/postgresql/17/bin/psql','-h','127.0.0.1','-U','postgres','-d','billiards_db','-v','ON_ERROR_STOP=1','-c',sql],check=True,stdout=subprocess.DEVNULL)
try:
 a,an=login('a');b,bn=login('b');c,cn=login('c');d,dn=login('d')
 print('PASS registration and mixed-case username login',flush=True)
 for target in ['  ',an.upper(),'no_such_'+prefix]:
  challenge(a,target);assert expect(a,45)[1] is False
 challenge(a,'  '+bn.upper()+'  ');assert expect(b,43)[1]==an
 response(b,' '+an.upper()+' ',False);assert expect(a,45)[1] is False
 print('PASS invalid challenge rejection and case-insensitive decline',flush=True)
 send(a,5);challenge(a,bn);expect(b,43);response(b,an,True)
 assert expect(a,6)[1]==bn;assert expect(b,6)[1]==an
 send(c,5);assert quiet(c);send(d,5);expect(c,6);expect(d,6)
 print('PASS accepted challenge creates room; inviter removed from queue',flush=True)
 send(a,8);send(a,8);assert quiet(a,.3) and quiet(b,.3)
 send(b,8);sa=expect(a,9);sb=expect(b,9);assert sa[3]==sb[3]
 print('PASS duplicate READY from one player does not start game; distinct second player starts it',flush=True)
 for i in range(60):
  angle=math.atan2(math.sin(i*.35),math.cos(i*.35))
  send(a,47,struct.pack('>?4d',True,angle,.5,0.,0.));got=expect(b,47)
  assert got[1] and abs(got[2]-angle)<1e-10
  time.sleep(.034)
 assert quiet(a)
 print('PASS 60 consecutive 34-byte aim samples relayed; no echo; not a two-UI benchmark',flush=True)
 for bad in [float('nan'),float('inf'),-float('inf')]:
  send(a,47,struct.pack('>?4d',True,bad,.5,0.,0.));assert quiet(b)
  send(a,10,struct.pack('>6d',bad,10.,200.,200.,0.,0.));assert quiet(b)
 send(a,10,struct.pack('>6d',.7,-1.,200.,200.,0.,0.));assert quiet(b)
 print('PASS non-finite aim/shot angles and negative shot force rejected in tested inputs',flush=True)
 shot=struct.pack('>6d',.7,10.,200.,200.,0.,0.)
 send(a,10,shot);expect(b,11);assert quiet(a)
 send(a,10,shot);assert quiet(b)
 send(a,47,struct.pack('>?4d',True,.7,.5,0.,0.));assert quiet(b)
 print('PASS duplicate shot and aim during active shot rejected',flush=True)
 send(a,20,struct.pack('>??',False,False));ta=expect(a,12);tb=expect(b,12);assert not ta[1] and tb[1]
 print('PASS PHYSICS_DONE switches turn; no board snapshot/ACK',flush=True)
 send(a,22,struct.pack('>i',0)+st('#DEB887'));send(a,18,struct.pack('>q',123456))
 expect(a,23);assert expect(a,18)[1]==123456
 send(a,40,st(bn));history=expect(a,41);assert history[1]==bn
 print('PASS boolean SETTINGS_RESPONSE and CHAT_HISTORY_RESPONSE friend+history wire format',flush=True)
 # Bounded characterization of remaining validation gaps; these are not acceptance passes.
 send(b,20,struct.pack('>??',False,False));assert expect(a,12)[1] and not expect(b,12)[1]
 print('FINDING current player can send PHYSICS_DONE before any shot and change turn',flush=True)
 send(a,10,struct.pack('>6d',.7,1000.,200.,200.,0.,0.));assert expect(b,11)[2]==1000.
 print('FINDING finite force 1000 above client UI maximum 25 is relayed; finite validation is not range validation',flush=True)
 send(a,20,struct.pack('>??',True,False));expect(a,12);expect(b,12)
 e,en=login('e');f,fn=login('f')
 challenge(e,fn);expect(f,43);start=time.monotonic();e.settimeout(18);assert expect(e,45)[1] is False
 elapsed=time.monotonic()-start;assert 14<elapsed<18
 print('PASS unanswered challenge expires after %.2f seconds'%elapsed,flush=True)
 challenge(e,fn);expect(f,43);time.sleep(.25);response(f,'wrong_'+prefix,False)
 assert expect(e,45)[1] is False
 challenge(e,fn);expect(f,43);response(f,en,False);assert expect(e,45)[1] is False
 print('PASS mismatched reply preserves expiry; target can receive next invitation',flush=True)
 challenge(e,fn);expect(f,43);f.close();assert expect(e,45)[1] is False
 g,gn=login('g');h,hn=login('h');challenge(g,hn);expect(h,43);g.close();time.sleep(.15);response(h,gn,True);assert expect(h,45)[1] is False
 print('PASS pending challenge cleanup on recipient/challenger disconnect',flush=True)
 q=socket.create_connection(('127.0.0.1',12355),timeout=5);clients.append(q)
 send(q,3,st('invalid-email')+st(prefix+'_bad')+st('ReportCurrent2026'));assert expect(q,4)[1] is False
 t,tn=login('tld',email=prefix+'@example.technology')
 print('PASS server rejects malformed email and accepts long valid TLD with email login',flush=True)
finally:
 cleanup();print('Temporary test accounts and related rows removed',flush=True)
