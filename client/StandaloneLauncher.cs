using System;
using System.IO;
using System.IO.Compression;
using System.Diagnostics;
using System.Reflection;
using System.Windows.Forms;

namespace BilliardsLauncher
{
    static class Program
    {
        [STAThread]
        static void Main(string[] args)
        {
            try
            {
                string appData = Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData);
                string gameDir = Path.Combine(appData, "BilliardsClubGame");
                string exePath = Path.Combine(gameDir, "BilliardsClient.exe");
                string versionFile = Path.Combine(gameDir, ".build_ver");

                Assembly currentAssembly = Assembly.GetExecutingAssembly();
                string resourceName = "GameArchive.zip";

                using (Stream stream = currentAssembly.GetManifestResourceStream(resourceName))
                {
                    if (stream == null)
                    {
                        MessageBox.Show("Lỗi: Không tìm thấy gói dữ liệu game bên trong file thực thi!", 
                                        "8-Ball Pool Launcher", MessageBoxButtons.OK, MessageBoxIcon.Error);
                        return;
                    }

                    long currentSize = stream.Length;
                    bool needExtract = true;

                    if (File.Exists(exePath) && File.Exists(versionFile))
                    {
                        try
                        {
                            string savedVer = File.ReadAllText(versionFile).Trim();
                            bool hasDlls = File.Exists(Path.Combine(gameDir, @"runtime\bin\prism_d3d.dll")) || 
                                           File.Exists(Path.Combine(gameDir, @"prism_d3d.dll"));
                            if (savedVer == currentSize.ToString() && hasDlls)
                            {
                                needExtract = false;
                            }
                        }
                        catch {}
                    }

                    if (needExtract)
                    {
                        if (!Directory.Exists(gameDir))
                        {
                            Directory.CreateDirectory(gameDir);
                        }

                        string tempZip = Path.Combine(Path.GetTempPath(), "billiards_pkg_" + Guid.NewGuid().ToString("N") + ".zip");
                        using (FileStream fs = new FileStream(tempZip, FileMode.Create, FileAccess.Write))
                        {
                            stream.CopyTo(fs);
                        }

                        using (ZipArchive archive = ZipFile.OpenRead(tempZip))
                        {
                            foreach (ZipArchiveEntry entry in archive.Entries)
                            {
                                string destPath = Path.Combine(gameDir, entry.FullName);
                                if (string.IsNullOrEmpty(entry.Name))
                                {
                                    Directory.CreateDirectory(destPath);
                                }
                                else
                                {
                                    string dir = Path.GetDirectoryName(destPath);
                                    if (!string.IsNullOrEmpty(dir)) Directory.CreateDirectory(dir);
                                    entry.ExtractToFile(destPath, true);
                                }
                            }
                        }

                        try { File.Delete(tempZip); } catch {}
                        try { File.WriteAllText(versionFile, currentSize.ToString()); } catch {}
                    }
                }

                if (!File.Exists(exePath))
                {
                    MessageBox.Show("Không tìm thấy file khởi động game tại: " + exePath, 
                                    "8-Ball Pool Launcher", MessageBoxButtons.OK, MessageBoxIcon.Error);
                    return;
                }

                ProcessStartInfo psi = new ProcessStartInfo();
                psi.FileName = exePath;
                psi.WorkingDirectory = gameDir;
                if (args != null && args.Length > 0)
                {
                    psi.Arguments = string.Join(" ", args);
                }
                psi.UseShellExecute = false;

                Process p = Process.Start(psi);
                if (p != null)
                {
                    p.WaitForExit();
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("Có lỗi xảy ra khi khởi động game:\n" + ex.Message, 
                                "8-Ball Pool Launcher", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }
    }
}
