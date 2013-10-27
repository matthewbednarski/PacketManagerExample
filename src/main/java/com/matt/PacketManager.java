package com.matt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;

import com.matt.artifact.message.Message;
import com.matt.artifact.packet.Packet;
import com.matt.artifact.packet.Packets;

@Path("packet")
public class PacketManager {

	@Context
	private MessageContext context;

	
	private static Logger logger;
	private static Logger log() {
		if (logger == null) {
			logger = Logger.getLogger(Thread.currentThread().getStackTrace()[0].getClassName());
		}
		return logger;
	}
	
	private PacketManager.Utils _utils;
	private PacketManager.Utils getUtils() {
		if (_utils == null) {
			_utils = new PacketManager.Utils();
		}
		return _utils;
	}
	@Path("")
	@GET
	@Produces({
	MediaType.TEXT_XML, MediaType.APPLICATION_JSON
	})
	public Packets getOSes() {
		Packets p = new Packets();
		for (String s : getUtils().getOses()) {
			Packet pp = new Packet();
			pp.setName(s);
			pp.setUri(getUtils().getOsesURI(s));
			p.getPacket().add(pp);
		}

		return p;
	}
	@Path("{os}")
	@GET
	@Produces({
	MediaType.TEXT_XML, MediaType.APPLICATION_JSON
	})
	public Packets getArchs(@PathParam("os") String os) {
		Packets p = new Packets();
		for (String s : getUtils().getArchs(os)) {
			Packet pp = new Packet();
			pp.setName(s);
			pp.setUri(getUtils().getArchsURI(os, s));
			p.getPacket().add(pp);
		}

		return p;
	}
	@Path("{os}/{arch}")
	@GET
	@Produces({
	MediaType.TEXT_XML, MediaType.APPLICATION_JSON
	})
	public Packets getApps(@PathParam("os") String os,
			@PathParam("arch") String arch) {
		Packets p = new Packets();
		for (String s : getUtils().getApps(os, arch)) {
			Packet pp = new Packet();
			pp.setName(s);
			pp.setUri(getUtils().getAppsURI(os, arch, s));
			p.getPacket().add(pp);
		}
		return p;
	}
	@Path("{os}/{arch}/{name}")
	@GET
	@Produces({
	MediaType.TEXT_XML, MediaType.APPLICATION_JSON
	})
	public Packets getVersions(@PathParam("os") String os,
			@PathParam("arch") String arch, @PathParam("name") String name) {
		Packets p = new Packets();
		for (String s : getUtils().getVersions(os, arch, name)) {
			Packet pp = new Packet();
			pp.setName(s);
			pp.setUri(getUtils().getVersionURI(os, arch, name, s));
			p.getPacket().add(pp);
		}

		return p;
	}
	@Path("{os}/{arch}/{name}/{version}")
	@GET
	@Produces({
	"multipart/mixed;type=text/xml", MediaType.APPLICATION_JSON
	})
	public Packets getPacket(@PathParam("os") String os,
			@PathParam("arch") String arch, @PathParam("name") String name,
			@PathParam("version") String version) {
		Packets p = new Packets();
		for (String s : getUtils().getVersions(os, arch, name)) {
			if (s.toLowerCase().equals(version)) {
				File pFile = new File(getUtils().getFSDir(), os);
				pFile = new File(pFile, arch);
				pFile = new File(pFile, name);
				pFile = new File(pFile, version);
				pFile = pFile.listFiles()[0];
				Packet pp = new Packet();
				pp.setName(name);
				pp.setUri(getUtils().getPacketURI(os, arch, name, version, pFile.getName()));
				DataSource ds = new FileDataSource(pFile);
				pp.setData(new DataHandler(ds));
				pp.setArch(arch);
				// pp.setDateCreated(value);
				// pp.setDateModified(value);
				pp.setVersion(version);
				pp.setOs(os);
				
				// pp.setUri(value);
				// pp.setImages(value);

				p.getPacket().add(pp);
			}
		}

		return p;
	}
	@Path("add")
	@POST
	@Consumes("multipart/mixed;type=text/xml")
	@Produces({
	MediaType.TEXT_XML, MediaType.APPLICATION_JSON
	})
	public Message addPackets(Packets pp) {
		Message msg = null;

		StringBuilder sb = new StringBuilder();
		for (Packet p : pp.getPacket()) {
			File f = new File(getUtils().getFSDir());
			f = new File(f, p.getOs());
			if (f.exists() && f.isDirectory()) {
				f = new File(f, p.getArch());
				if (f.exists() && f.isDirectory()) {
					f = new File(f, p.getName());
					if (!f.exists()) {
						try {
							FileUtils.forceMkdir(f);
						} catch (IOException e) {
							log().log(Level.ALL, e.getLocalizedMessage(), e);
						}
					}
					f = new File(f, p.getVersion());
					if (!f.exists()) {
						try {
							FileUtils.forceMkdir(f);
						} catch (IOException e1) {
							log().log(Level.ALL, e1.getLocalizedMessage(), e1);
						}
						f = new File(f, p.getName() + "_" + p.getVersion());
						if (f.exists()) {
							String fileName = f.getAbsolutePath();
							f.delete();
							f = new File(fileName);
						}
						DataHandler dh = p.getData();
						InputStream is = null;
						OutputStream os = null;
						try {
							is = dh.getInputStream();
							byte[] bytes = IOUtils.toByteArray(is);
							os = new FileOutputStream(f);
							os.write(bytes);
							bytes = null;
							sb.append(String.format("Packet %s, version %s added successfully.", p.getName(), p.getVersion()));
							sb.append(System.getProperty("line.separator"));
						} catch (IOException e) {
							log().log(Level.ALL, e.getLocalizedMessage(), e);
							sb.append(String.format("Packet %s, version %s could not be added.", p.getName(), p.getVersion()));
							sb.append(System.getProperty("line.separator"));
						}

						finally {
							IOUtils.closeQuietly(is);
							IOUtils.closeQuietly(os);
						}

					}
				}
			}
		}
		msg = getNewMessage(sb.toString(), getUtils().now());

		return msg;
	}
	// @Path("list/{os}/{arch}/{name}")
	// @GET
	// @Produces({
	// "multipart/mixed;type=text/xml", MediaType.APPLICATION_JSON
	// })
	// public Packets getAppsVersions(@PathParam("os") String
	// os,@PathParam("arch") String arch,@PathParam("name") String name) {
	// Packets p = new Packets();
	// for(String s : getUtils().getApps(os, arch)){
	// Packet pp = new Packet();
	// pp.setName(s);
	// p.getPacket().add(pp);
	// }
	//
	// return p;
	// }
	@Path("setup")
	@GET
	@Produces({
	MediaType.TEXT_XML, MediaType.APPLICATION_JSON
	})
	public Message setup() {
		getUtils().SetupFSDB();
		return getNewMessage("Finished Setup", getUtils().now());
	}
	private static Message getNewMessage(String message,
			XMLGregorianCalendar xcal) {
		Message m = new Message();
		m.setDateTime(xcal);
		m.setSender(PacketManager.class.getName());
		Message.Data d = new Message.Data();
		d.setText(message);
		m.setData(d);
		return m;
	}
	private Packets getPackets(String os, String arch, String name,
			String version, Integer length) {
		Packets p = new Packets();

		boolean is_v;
		boolean is_length;
		boolean is_arch;
		boolean is_os;
		boolean is_name;

		if (!StringUtils.isBlank(name)) {
			is_name = true;
		}
		if (!StringUtils.isBlank(version)) {
			is_v = true;
		}
		if (!StringUtils.isBlank(os)) {
			is_os = true;
		}
		if (!StringUtils.isBlank(arch)) {
			is_arch = true;
		}

		return p;
	}


	@Path("logout")
	@GET	
//	@Produces({
//		MediaType.TEXT_XML, MediaType.APPLICATION_JSON
//		})
	public Response logoutCurrentUser(){
		Response r = Response.status(Response.Status.UNAUTHORIZED).build();
		return r;
	}
	
			
	static class Utils {
		static final String FS_DB_DIR = "fs_db_dir";
		private String _fs_dir;
		public String getFSDir() {
			try {
				_fs_dir = (String) getEnvCtx().lookup(FS_DB_DIR);
			} catch (NamingException e) {
				log().log(Level.WARNING, e.getLocalizedMessage(), e);
			}
			return _fs_dir;
		}
		private javax.naming.Context envCtx;
		private javax.naming.Context getEnvCtx() {
			if (envCtx == null) {
				try {
					envCtx = (javax.naming.Context) new InitialContext().lookup("java:comp/env");
				} catch (NamingException e) {
					e.printStackTrace();
				}
			}
			return envCtx;
		}
		private void SetupFSDB() {

			File dir = new File(getFSDir());
			File tmp = new File(dir, "fs_db_setup.done");
			if (!tmp.exists()) {
				if (dir.exists()) {
					if (dir.isDirectory()) {
						log().log(Level.FINE, "Got Directory");
						SetupFSDB_OSs(dir);
					} else {
						log().log(Level.SEVERE, "File with name \"%s\" exists but is not a directory, please reconfigure web.xml environment variable \"%s\".", new String[] {
						dir.getAbsolutePath(), FS_DB_DIR
						});
					}
				} else {
					log().log(Level.FINE, "Directory does not exist");
					try {
						FileUtils.forceMkdir(dir);
						SetupFSDB_OSs(dir);
					} catch (IOException e) {
						log().log(Level.FINE, e.getLocalizedMessage(), e);
					}
				}
			}

		}
		private void SetupFSDB_OSs(File dir) {
			String os[] = new String[] {
			"Windows", "Linux", "BSD", "OSx", "AIX"
			};
			for (String osName : os) {
				File osDir = new File(dir, osName);
				if (!osDir.exists()) {
					try {
						FileUtils.forceMkdir(osDir);
						SetupFSDB_Archs(osDir);
					} catch (IOException e) {
						log().log(Level.FINE, e.getLocalizedMessage(), e);
					}
				} else {
					SetupFSDB_Archs(osDir);
				}
			}
			if (dir.isDirectory()) {
				File tmp = new File(dir, "fs_db_setup.done");
				try {
					tmp.createNewFile();
				} catch (IOException e) {
					log().log(Level.FINE, e.getLocalizedMessage(), e);
				}
			}
		}
		private void SetupFSDB_Archs(File os) {
			String arch[] = new String[] {
			"x86", "x64"
			};
			for (String archName : arch) {
				File archDir = new File(os, archName);
				if (!archDir.exists()) {
					try {
						FileUtils.forceMkdir(archDir);
					} catch (IOException e) {
						log().log(Level.FINE, e.getLocalizedMessage(), e);
					}
				}
			}
		}
		public XMLGregorianCalendar now() {
			GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
			XMLGregorianCalendar xgcal = null;
			try {
				xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
			} catch (DatatypeConfigurationException e) {
				log().log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
			return xgcal;
		}

		// eventually add FileSystem monitor to allow for creation of list only
		// upon changes to File System
		private List<File> files;
		public Map<String, File> getFiles() {
			Map<String, File> r = new HashMap<String, File>();
			List<String> fileNames = getFilesRecurse(this.getFSDir());
			for (String s : fileNames) {
				r.put(s, new File(s));
			}
			return r;
		}
		private static List<String> getFilesRecurse(String start) {
			List<String> r = new ArrayList<String>();

			File f = new File(start);
			String l[] = f.list();
			for (String s : l) {
				File fd = new File(f, s);
				if (!fd.isDirectory()) {
					r.add(fd.getAbsolutePath());
				} else {
					r.addAll(getFilesRecurse(fd.getAbsolutePath()));
				}
			}

			return r;
		}
		public List<String> getOses() {
			List<String> r = new ArrayList<String>();
			File osDir = new File(getFSDir());
			File[] files = osDir.listFiles();
			Arrays.sort(files);
			for (File app : files) {
				if (app.isDirectory()) {
					r.add(app.getName());
				}
			}
			return r;
		}
		public String getOsesURI(String os) {
			String s = String.format("/%s", os );
			
			return s;
		}
		public List<String> getArchs(String os) {
			List<String> r = new ArrayList<String>();
			File archDir = new File(getFSDir(), os);
			File[] files = archDir.listFiles();
			Arrays.sort(files);
			for (File app : files) {
				if (app.isDirectory()) {
					r.add(app.getName());
				}
			}
			return r;
		}
		public String getArchsURI(String os, String arch) {
			String s = String.format("/%s/%s", os, arch);
			
			return s;
		}
		public List<String> getApps(String os, String arch) {
			List<String> r = new ArrayList<String>();
			File appDir = new File(getFSDir(), os);
			appDir = new File(appDir, arch);
			File[] files = appDir.listFiles();
			Arrays.sort(files);
			for (File app : files) {
				if (app.isDirectory()) {
					r.add(app.getName());
				}
			}
			return r;
		}
		public String getAppsURI(String os, String arch, String name) {
			String s = String.format("/%s/%s/%s", os, arch, name);
			
			return s;
		}
		public List<String> getVersions(String os, String arch, String name) {
			List<String> r = new ArrayList<String>();
			File versionDir = new File(getFSDir(), os);
			versionDir = new File(versionDir, arch);
			versionDir = new File(versionDir, name);
			File[] files = versionDir.listFiles();
			Arrays.sort(files);
			for (File app : files) {
				if (app.isDirectory()) {
					r.add(app.getName());
				}
			}
			return r;
		}
		public String getVersionURI(String os, String arch, String name, String version) {
			String s = String.format("/%s/%s/%s/%s", os, arch, name, version);
			
			return s;
		}
		public String getPacketURI(String os, String arch, String name, String version, String packetName) {
			String s = String.format("/%s/%s/%s/%s/%s", os, arch, name, version, packetName);
			
			return s;
		}
		public Map<String, File> getFilesOs(String os, Map<String, File> map) {
			Map<String, File> r = new HashMap<String, File>();
			File osDir = new File(getFSDir(), os);

			for (String key : map.keySet()) {
				if (key.startsWith(osDir.getAbsolutePath())) {
					r.put(key, map.get(key));
				}
			}
			return r;
		}

		public Map<String, File> getFilesOsArch(String os, String arch,
				Map<String, File> map) {
			Map<String, File> r = getFilesOs(os, map);
			File archDir = new File(getFSDir(), os);
			archDir = new File(archDir, arch);

			for (String key : map.keySet()) {
				if (key.startsWith(archDir.getAbsolutePath())) {
					r.put(key, map.get(key));
				}
			}
			return r;
		}
		public Map<String, File> getFilesOsArchName(String os, String arch,
				String name, Map<String, File> map) {
			Map<String, File> r = getFilesOs(os, map);
			File appDir = new File(getFSDir(), os);
			appDir = new File(appDir, arch);
			appDir = new File(appDir, name);

			for (String key : map.keySet()) {
				if (key.startsWith(appDir.getAbsolutePath())) {
					r.put(key, map.get(key));
				}
			}
			return r;
		}
		public Map<String, File> getFilesOsArchNameVersion(String os,
				String arch, String name, String version, Map<String, File> map) {
			Map<String, File> r = getFilesOs(os, map);
			File appDir = new File(getFSDir(), os);
			appDir = new File(appDir, arch);
			appDir = new File(appDir, name);
			appDir = new File(appDir, version);

			for (String key : map.keySet()) {
				if (key.startsWith(appDir.getAbsolutePath())) {
					r.put(key, map.get(key));
				}
			}
			return r;
		}
		public Map<String, File> getFilesMap(String os, String arch,
				String search) {
			Map<String, File> map = getFiles();
			Map<String, File> r = getFilesOsArch(os, arch, map);
			File archDir = new File(getFSDir(), os);
			archDir = new File(archDir, arch);

			for (String key : map.keySet()) {
				if (key.startsWith(archDir.getAbsolutePath()) && StringUtils.remove(key, archDir.getAbsolutePath()).toLowerCase().contains(search)) {
					r.put(key, map.get(key));
				}
			}
			return r;
		}
		public byte[] getMD5(File f) {
			byte[] digest = null;
			MessageDigest md = null;
			try (InputStream is = Files.newInputStream(Paths.get(f.getAbsolutePath()))) {
				md = MessageDigest.getInstance("MD5");

				DigestInputStream dis = new DigestInputStream(is, md);
				/* Read stream to EOF as normal... */

				digest = md.digest();
			} catch (NoSuchAlgorithmException | IOException e) {}
			return digest;
		}
	}
}
