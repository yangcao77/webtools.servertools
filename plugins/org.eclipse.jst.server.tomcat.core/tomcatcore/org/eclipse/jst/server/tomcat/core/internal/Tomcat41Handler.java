/*******************************************************************************
 * Copyright (c) 2003, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jst.server.tomcat.core.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IModule;
/**
 * Tomcat 41 handler.
 */
public class Tomcat41Handler implements ITomcatVersionHandler {
	/**
	 * @see ITomcatVersionHandler#verifyInstallPath(IPath)
	 */
	public IStatus verifyInstallPath(IPath installPath) {
		IStatus result = TomcatVersionHelper.checkCatalinaVersion(installPath, TomcatPlugin.TOMCAT_41);
		// If check was canceled, use folder check
		if (result.getSeverity() == IStatus.CANCEL) {
			result = TomcatPlugin.verifyInstallPathWithFolderCheck(installPath, TomcatPlugin.TOMCAT_41);
		}
		return result;
	}
	
	/**
	 * @see ITomcatVersionHandler#getRuntimeClass()
	 */
	public String getRuntimeClass() {
		return "org.apache.catalina.startup.Bootstrap";
	}
	
	/**
	 * @see ITomcatVersionHandler#getRuntimeClasspath(IPath, IPath)
	 */
	public List getRuntimeClasspath(IPath installPath, IPath configPath) {
		List<IRuntimeClasspathEntry> cp = new ArrayList<IRuntimeClasspathEntry>();
		
		// 4.1 - add bootstrap.jar from the Tomcat bin directory
		IPath binPath = installPath.append("bin");
		if (binPath.toFile().exists()) {
			IPath path = binPath.append("bootstrap.jar");
			cp.add(JavaRuntime.newArchiveRuntimeClasspathEntry(path));
		}
		
		return cp;
	}

	/**
	 * @see ITomcatVersionHandler#getRuntimeProgramArguments(IPath, boolean, boolean)
	 */
	public String[] getRuntimeProgramArguments(IPath configPath, boolean debug, boolean starting) {
		List<String> list = new ArrayList<String>();
		
		if (debug)
			list.add("-debug");
		
		if (starting)
			list.add("start");
		else
			list.add("stop");
		
		String[] temp = new String[list.size()];
		list.toArray(temp);
		return temp;
	}

	/**
	 * @see ITomcatVersionHandler#getExcludedRuntimeProgramArguments(boolean, boolean)
	 */
	public String[] getExcludedRuntimeProgramArguments(boolean debug, boolean starting) {
		if (!debug) {
			return new String [] { "-debug" };
		}
		return null;
	}
	
	/**
	 * @see ITomcatVersionHandler#getRuntimeVMArguments(IPath, IPath, IPath, boolean)
	 */
	public String[] getRuntimeVMArguments(IPath installPath, IPath configPath, IPath deployPath, boolean isTestEnv) {
		return TomcatVersionHelper.getCatalinaVMArguments(installPath, configPath, deployPath, getEndorsedDirectories(installPath), isTestEnv);
	}

	/**
	 * @see ITomcatVersionHandler#getRuntimePolicyFile(IPath)
	 */
	public String getRuntimePolicyFile(IPath configPath) {
		return configPath.append("conf").append("catalina.policy").toOSString();
	}

	/**
	 * @see ITomcatVersionHandler#canAddModule(IModule)
	 */
	public IStatus canAddModule(IModule module) {
		String version = module.getModuleType().getVersion();
		if ("2.2".equals(version) || "2.3".equals(version))
			return Status.OK_STATUS;
		
		return new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, Messages.errorSpec41, null);
	}

	/**
	 * @see ITomcatVersionHandler#getRuntimeBaseDirectory(TomcatServer)
	 */
	public IPath getRuntimeBaseDirectory(TomcatServer server) {
		return TomcatVersionHelper.getStandardBaseDirectory(server);
	}

	/**
	 * @see ITomcatVersionHandler#prepareRuntimeDirectory(IPath)
	 */
	public IStatus prepareRuntimeDirectory(IPath baseDir) {
		return TomcatVersionHelper.createCatalinaInstanceDirectory(baseDir);
	}

	/**
	 * @see ITomcatVersionHandler#prepareDeployDirectory(IPath)
	 */
	public IStatus prepareDeployDirectory(IPath deployPath) {
		return TomcatVersionHelper.createDeploymentDirectory(deployPath,
				TomcatVersionHelper.DEFAULT_WEBXML_SERVLET23);
	}
	
	/**
	 * @see ITomcatVersionHandler#prepareForServingDirectly(IPath, TomcatServer)
	 */
	public IStatus prepareForServingDirectly(IPath baseDir, TomcatServer server, String tomcatVersion) {
		if (server.isServeModulesWithoutPublish())
			return new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, Messages.errorNoPublishNotSupported, null);
		return Status.OK_STATUS;
	}	
	
	/**
	 * @see ITomcatVersionHandler#getSharedLoader(IPath)
	 */
	public String getSharedLoader(IPath baseDir) {
		// Not supported
		return null;
	}
	
	/**
	 * Returns false since Tomcat 4.1 doesn't support this feature.
	 * 
	 * @return false since feature is not supported
	 */
	public boolean supportsServeModulesWithoutPublish() {
		return false;
	}

	/**
	 * @see ITomcatVersionHandler#supportsDebugArgument()
	 */
	public boolean supportsDebugArgument() {
		return true;
	}

	/**
	 * While Tomcat 4.1.x does implement a form of separate
	 * context files, these file are found outside of the
	 * &quot;conf&quot; directory and is not practical to
	 * support.
	 * @see ITomcatVersionHandler#supportsSeparateContextFiles()
	 */
	public boolean supportsSeparateContextFiles() {
		return false;
	}
	
	/**
	 * @see ITomcatVersionHandler#getEndorsedDirectories(IPath)
	 */
	public String getEndorsedDirectories(IPath installPath) {
		return installPath.append("common").append("endorsed").toOSString();
	}	
}
