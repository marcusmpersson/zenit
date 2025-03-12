package main.java.zenit.javacodecompiler;

import main.java.zenit.Zenit;
import main.java.zenit.filesystem.jreversions.JDKVerifier;
import main.java.zenit.filesystem.jreversions.JREVersions;

import java.io.File;
import java.util.regex.Matcher;

public class CommandBuilder {

	public static final String RUN = "java";
	public static final String COMPILE = "javac";

	String tool;
	String JDK;
	String directory;
	String sourcepath;
	String[] internalLibraries;
	String[] externalLibraries;
	String[] libraries;
	String runPath;
	String programArguments;
	String VMArguments;

	public CommandBuilder(String tool) {
		this.tool = tool;
	}
	
	public void setJDK(String JDK) {
		//If project has special JDK
		if (JDK != null) {
			this.JDK = JDKVerifier.getExecutablePath(JDK, tool);
		//If default JDK is set
		} else {
			String defaultJDK = JREVersions.getDefaultJDKFile().getPath();
			if (defaultJDK != null) {
				this.JDK = JDKVerifier.getExecutablePath(defaultJDK, tool);
			}
		}
		
		//If no default JDK is set
		if (this.JDK == null) {
			this.JDK = tool;
		}
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public void setSourcepath(String sourcepath) {
		this.sourcepath = "-sourcepath " + sourcepath;
	}

	public void setInternalLibraries(String[] internalLibraries) {
		this.internalLibraries = internalLibraries;
	}
	
	public void setExternalLibraries(String[] externalLibraries) {
		this.externalLibraries = externalLibraries;
	}

	public void setRunPath(String runPath) {
		this.runPath = runPath;
	}
	
	public void setProgramArguments(String programArguments) {
		this.programArguments = programArguments;
	}
	
	public void setVMArguments(String VMArguments) {
		this.VMArguments = VMArguments;
	}
	
	private void mergeLibraries() {
		int intLength = 0;
		int extLength = 0;
		
		if (internalLibraries == null && externalLibraries == null) {
			return;
		}
		
		if (internalLibraries != null) {
			intLength = internalLibraries.length;
		}
		if (externalLibraries != null) {
			extLength = externalLibraries.length;
		}
		libraries = new String[intLength + extLength];
		for (int i = 0; i < libraries.length; i++) {
			if (i < intLength) {
				libraries[i] = internalLibraries[i];
			} else {
				libraries[i] = externalLibraries[i-intLength];
			}
		}
	}

	public String generateCommand() {
		String os = Zenit.OS.toLowerCase();
		boolean isWindows = os.startsWith("win");
		return buildCommand(isWindows);
	}

	private String buildCommand(boolean isWindows) {
		StringBuilder command = new StringBuilder();

		if (isWindows) {
			command.append("\"").append(JDK).append("\"");
		} else {
			command.append(JDK);
		}

		if (VMArguments != null) {
			command.append(" ").append(VMArguments);
		}

		mergeLibraries();
		String separator = isWindows ? ";" : ":";
		command.append(" -cp ");

		if (isWindows) {
			command.append("\"");
		}

		if (tool.equals(RUN)) {
			command.append(isWindows ? directory + separator : "./" + directory + separator);
		}

		if (libraries != null && libraries.length > 0) {
			command.append(String.join(separator, libraries)).append(separator);
		}

		command.append(".");

		if (isWindows) {
			command.append("\"");
		}

		if (directory != null && tool.equals(COMPILE)) {
			command.append(" -d ").append(directory);
		}

		if (sourcepath != null) {
			command.append(" ").append(sourcepath);
		}

		if (runPath != null && tool.equals(RUN)) {
			command.append(" ").append(runPath.replace(File.separator, "/"));
		}

		if (tool.equals(COMPILE)) {
			command.append(" ").append(runPath);
		}

		if (programArguments != null) {
			command.append(" ").append(programArguments);
		}
		return command.toString();
	}

	@Deprecated
	public String generateCommandOld() {
		String command = JDK;
		
		if (VMArguments != null) {
			command += " " + VMArguments;
		}
		
		mergeLibraries();
		
		if(tool.equals(RUN) && directory != null) {
			command += " -cp " + directory;
		}

		if (libraries != null) {
			if (tool.equals(COMPILE)) {
				command += " -cp " + libraries[0];
				if (libraries.length > 1) {
					for (int i = 1; i < libraries.length; i++) {
						command += File.pathSeparatorChar + libraries[i];
					}
				}
			} else if (tool.equals(RUN)) {
				for (int i = 0; i < libraries.length; i++) {
					command += File.pathSeparatorChar + libraries[i];
				}
				//command += ":.";
			}
		}

		if (directory != null && tool.equals(COMPILE)) {
			command += " -d " + directory;
		}
		if (sourcepath != null) {
			command += " " + sourcepath;
		}

		if (runPath != null) {
			if (tool.equals(RUN)) {
				runPath = runPath.replaceAll(Matcher.quoteReplacement(File.separator), "/");
			}
			command += " " + runPath;
		}
		
		if (programArguments != null) {
			command += " " + programArguments;
		}

		return command;
	}
}