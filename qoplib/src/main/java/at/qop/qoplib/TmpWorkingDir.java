package at.qop.qoplib;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

public class TmpWorkingDir {
	
	public File dir;
	
	public void create()
	{
		String wdPath = ConfigFile.read().getWorkingDir();
		File wd = new File(wdPath);
		
		if (!wd.isDirectory())
		{
			throw new RuntimeException("!wd.isDirectory()");
		}
		
		dir = new File(wd , "qoptmp" + new Date() + "");
		dir.deleteOnExit();
		dir.mkdir();
	}
	
	public void cleanUp() throws IOException {
		Path directory = getPath();
		Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
		   @Override
		   public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		       Files.delete(file);
		       return FileVisitResult.CONTINUE;
		   }

		   @Override
		   public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		       Files.delete(dir);
		       return FileVisitResult.CONTINUE;
		   }
		});
	}

	public Path getPath() {
		return Paths.get(dir + "");
	}

}
