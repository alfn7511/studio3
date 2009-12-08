package com.aptana.scripting.model;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.jruby.anno.JRubyMethod;

import com.aptana.scripting.Activator;
import com.aptana.scripting.ScriptingEngine;

public class BundleManager
{
	private static final String USER_BUNDLE_DIRECTORY_GENERAL = "RadRails Red Bundles"; //$NON-NLS-1$
	private static final String USER_BUNDLE_DIRECTORY_MACOSX = "/Documents/RadRails Red Bundles"; //$NON-NLS-1$
	private static final String BUNDLE_FILE = "bundle.rb"; //$NON-NLS-1$
	private static final String RUBY_FILE_EXTENSION = ".rb"; //$NON-NLS-1$
	private static final String BUNDLES_FOLDER_NAME = "bundles"; //$NON-NLS-1$
	private static final String SNIPPETS_FOLDER_NAME = "snippets"; //$NON-NLS-1$
	private static final String COMMANDS_FOLDER_NAME = "commands"; //$NON-NLS-1$
	private static final String USER_HOME_PROPERTY = "user.home"; //$NON-NLS-1$
	private static BundleManager INSTANCE;

	private static final String UNC_PREFIX = "//"; //$NON-NLS-1$
	private static final String SCHEME_FILE = "file"; //$NON-NLS-1$
	
	private List<Bundle> _bundles;
	private Map<String, Bundle> _bundlesByPath;
	
	/**
	 * getInstance
	 * 
	 * @return
	 */
	@JRubyMethod(name = "instance")
	public static BundleManager getInstance()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new BundleManager();
		}

		return INSTANCE;
	}

	/**
	 * BundleManager
	 */
	private BundleManager()
	{
	}

	/**
	 * addBundle
	 * 
	 * @param bundle
	 */
	@JRubyMethod(name = "add_bundle")
	public void addBundle(Bundle bundle)
	{
		if (bundle != null)
		{
			if (this._bundles == null)
			{
				this._bundles = new ArrayList<Bundle>();
			}

			this._bundles.add(bundle);

			if (this._bundlesByPath == null)
			{
				this._bundlesByPath = new HashMap<String, Bundle>();
			}

			this._bundlesByPath.put(bundle.getPath(), bundle);
		}
	}

	/**
	 * getBuiltinsLoadPath
	 * 
	 * @return
	 */
	private String getBuiltinsLoadPath()
	{
		URL url = FileLocator.find(Activator.getDefault().getBundle(), new Path(BUNDLES_FOLDER_NAME), null);
		String result = null;

		try
		{
			URL fileURL = FileLocator.toFileURL(url);
			// CAW: URIUtil is a 3.5+ class, so I copied the necessary util method over here
			URI fileURI = toURI(fileURL);	// Use Eclipse to get around Java 1.5 bug on Windows
			File file = new File(fileURI);

			result = file.getAbsolutePath();
		}
		catch (IOException e)
		{
			String message = MessageFormat.format(
				Messages.BundleManager_Cannot_Locate_Built_Ins_Directory,
				new Object[] { url.toString() }
			);

			Activator.logError(message, e);
		}
		catch (URISyntaxException e)
		{
			String message = MessageFormat.format(
				Messages.BundleManager_Malformed_Built_Ins_URI,
				new Object[] { url.toString() }
			);

			Activator.logError(message, e);
		}

		return result;
	}
	
	private static URI toURI(URL url) throws URISyntaxException {
		//URL behaves differently across platforms so for file: URLs we parse from string form
		if (SCHEME_FILE.equals(url.getProtocol())) {
			String pathString = url.toExternalForm().substring(5);
			//ensure there is a leading slash to handle common malformed URLs such as file:c:/tmp
			if (pathString.indexOf('/') != 0)
				pathString = '/' + pathString;
			else if (pathString.startsWith(UNC_PREFIX) && !pathString.startsWith(UNC_PREFIX, 2)) {
				//URL encodes UNC path with two slashes, but URI uses four (see bug 207103)
				pathString = ensureUNCPath(pathString);
			}
			return new URI(SCHEME_FILE, null, pathString, null);
		}
		try {
			return new URI(url.toExternalForm());
		} catch (URISyntaxException e) {
			//try multi-argument URI constructor to perform encoding
			return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
		}
	}
	
	/**
	 * Ensures the given path string starts with exactly four leading slashes.
	 */
	private static String ensureUNCPath(String path) {
		int len = path.length();
		StringBuffer result = new StringBuffer(len);
		for (int i = 0; i < 4; i++) {
			//	if we have hit the first non-slash character, add another leading slash
			if (i >= len || result.length() > 0 || path.charAt(i) != '/')
				result.append('/');
		}
		result.append(path);
		return result.toString();
	}

	/**
	 * getBundleFromPath
	 * 
	 * @param path
	 * @return
	 */
	@JRubyMethod(name = "bundle_from_path")
	public Bundle getBundleFromPath(String path)
	{
		Bundle result = null;

		if (this._bundlesByPath != null)
		{
			result = this._bundlesByPath.get(path);
		}

		return result;
	}

	/**
	 * getCommandsFromScope
	 * 
	 * @param scope
	 * @return
	 */
	public Command[] getCommandsFromScope(String scope)
	{
		if (this._bundles == null)
			return new Command[0];
		
		List<Command> result = new ArrayList<Command>();
		for (Bundle bundle : this._bundles)
		{
			for (Command command : bundle.getCommands())
			{
				if (command.getScopeSelector().matches(scope))
				{
					result.add(command);
				}
			}
		}
		return result.toArray(new Command[result.size()]);
	}
	
	/**
	 * getLoadPaths
	 * 
	 * @param resource
	 * @return
	 */
	private List<String> getLoadPaths(File resource)
	{
		File folder = (resource != null && resource.isDirectory()) ? resource : resource.getParentFile();
		List<String> loadPaths = new ArrayList<String>();
		File bundleFolder = folder.getParentFile();
		File bundlesFolder = bundleFolder.getParentFile();
		
		loadPaths.add(this.getBuiltinsLoadPath());
		loadPaths.add(bundlesFolder.getAbsolutePath());
		loadPaths.add(bundleFolder.getAbsolutePath());
		loadPaths.add("."); //$NON-NLS-1$
		
		return loadPaths;
	}

	/**
	 * getLoadPaths
	 * 
	 * @param resource
	 * @return
	 */
	private List<String> getLoadPaths(IResource resource)
	{
		return this.getLoadPaths(resource.getLocation().toFile());
	}

	/**
	 * getSnippetsFromScope
	 * 
	 * @param scope
	 * @return
	 */
	public Snippet[] getSnippetsFromScope(String scope)
	{
		if (this._bundles == null)
			return new Snippet[0];
		
		List<Snippet> result = new ArrayList<Snippet>();
		for (Bundle bundle : this._bundles)
		{
			for (Snippet snippet : bundle.getSnippets())
			{
				if (snippet.getScopeSelector().matches(scope))
				{
					result.add(snippet);
				}
			}
		}

		return result.toArray(new Snippet[result.size()]);
	}

	/**
	 * getUserBundlePath
	 * 
	 * @return
	 */
	public String getUserBundlePath()
	{
		String OS = Platform.getOS();
		String userHome = System.getProperty(USER_HOME_PROPERTY);
		String result = null;
		
		// TODO: define user bundle paths for other platforms
		if (OS.equals(Platform.OS_MACOSX))
		{
			result = userHome + USER_BUNDLE_DIRECTORY_MACOSX;
		}
		else
		{
			result = userHome + File.separator + USER_BUNDLE_DIRECTORY_GENERAL;
		}
		
		return result;
	}
	
	/**
	 * loadBundles
	 */
	public void loadBundles()
	{
		this.loadProjectBundles();
		this.loadUserBundles();
	}
	
	/**
	 * loadProjectBundles
	 */
	private void loadProjectBundles()
	{
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects())
		{
			this.processProject(project);
		}
	}
	
	/**
	 * loadUserBundles
	 */
	private void loadUserBundles()
	{
		String userBundlesPath = this.getUserBundlePath();
		
		if (userBundlesPath != null && userBundlesPath.length() > 0)
		{
			File userBundles = new File(userBundlesPath);
			
			if (userBundles.exists() && userBundles.isDirectory() && userBundles.canRead())
			{
				File[] bundles = userBundles.listFiles(new FileFilter()
				{
					public boolean accept(File pathname)
					{
						return pathname.isDirectory() && pathname.canRead();
					}
				});
				
				for (File bundle : bundles)
				{
					this.processBundle(bundle, true);
				}
			}
		}
	}

	/**
	 * moveBundle
	 * 
	 * @param oldFolder
	 * @param newFolder
	 */
	public void moveBundle(String oldFolder, String newFolder)
	{
		if (newFolder != null && newFolder.length() > 0)
		{
			Bundle bundle = this.getBundleFromPath(oldFolder);

			if (bundle != null)
			{
				// remove bundle path reference
				this._bundlesByPath.remove(oldFolder);

				// update bundle path
				bundle.moveTo(newFolder);

				// add new path reference
				this._bundlesByPath.put(newFolder, bundle);
			}
		}
	}

	/**
	 * processBundle
	 * 
	 * @param bundleRoot
	 * @param processChildren
	 */
	public void processBundle(IResource bundleRoot, boolean processChildren)
	{
		this.processBundle(bundleRoot.getLocation().toFile(), processChildren);
	}
	
	/**
	 * processBundle
	 * 
	 * @param bundleRoot
	 * @param processChildren
	 */
	public void processBundle(File bundleRoot, boolean processChildren)
	{
		String bundlePath = bundleRoot.getAbsolutePath();
		File bundleFile = new File(bundlePath + File.separator + BUNDLE_FILE);
		
		if (bundleFile.exists() && bundleFile.canRead())
		{
			String fullPath = bundleFile.getAbsolutePath();
			List<String> loadPaths = new ArrayList<String>();
			
			loadPaths.add(this.getBuiltinsLoadPath());
			loadPaths.add("."); //$NON-NLS-1$
			
			ScriptingEngine.getInstance().runScript(fullPath, loadPaths);
			
			if (processChildren)
			{
				// process snippets and command folders
				this.processFolder(new File(bundlePath + File.separator + SNIPPETS_FOLDER_NAME));
				this.processFolder(new File(bundlePath + File.separator + COMMANDS_FOLDER_NAME));
			}
		}
		else
		{
			System.out.println(Messages.BundleManager_Missing_Bundle_File + bundlePath);
		}
	}
	
	/**
	 * processFolder
	 * 
	 * @param folder
	 */
	private void processFolder(File folder)
	{
		if (folder != null && folder.isDirectory() && folder.canRead())
		{
			List<String> loadPaths = this.getLoadPaths(folder);
			File[] files = folder.listFiles(new FilenameFilter()
			{
				public boolean accept(File dir, String name)
				{
					return name.toLowerCase().endsWith(RUBY_FILE_EXTENSION);
				}
			});
			
			for (File file: files)
			{
				String fullPath = file.getAbsolutePath();
				
				ScriptingEngine.getInstance().runScript(fullPath, loadPaths);
			}
		}
	}

	/**
	 * processProject
	 * 
	 * @param project
	 */
	private void processProject(IProject project)
	{
		IFolder bundlesFolder = project.getFolder(BUNDLES_FOLDER_NAME);

		if (bundlesFolder != null)
		{
			try
			{
				for (IResource resource : bundlesFolder.members())
				{
					if (resource instanceof IFolder)
					{
						this.processBundle(resource, true);
					}
				}
			}
			catch (CoreException e)
			{
			}
		}
	}

	/**
	 * processFile
	 * 
	 * @param file
	 */
	public void processSnippetOrCommand(IResource file)
	{
		if (file != null)
		{
			if (file.getName().toLowerCase().endsWith(RUBY_FILE_EXTENSION))
			{
				List<String> loadPaths = this.getLoadPaths(file);
				String fullPath = file.getLocation().toPortableString();

				ScriptingEngine.getInstance().runScript(fullPath, loadPaths);
			}
		}
	}

	/**
	 * removeBundle
	 * 
	 * @param bundle
	 */
	public void removeBundle(Bundle bundle)
	{
		if (bundle != null)
		{
			if (this._bundles != null)
			{
				this._bundles.remove(bundle);
			}
		}
	}

	/**
	 * removeBundle
	 * 
	 * @param bundleFolder
	 */
	public void removeBundle(String bundleFolder)
	{
		Bundle bundle = this.getBundleFromPath(bundleFolder);

		if (bundle != null)
		{
			this.removeBundle(bundle);
		}
	}

	/**
	 * removeSnippetOrCommand
	 * 
	 * @param file
	 */
	public void removeSnippetOrCommand(IResource file)
	{
		if (file != null)
		{
			IContainer parentFolder = file.getParent();
			IContainer bundleFolder = parentFolder.getParent();
			Bundle bundle = this.getBundleFromPath(bundleFolder.getLocation().toPortableString());
			
			if (bundle != null)
			{
				if (parentFolder.getName().equals(SNIPPETS_FOLDER_NAME))
				{
					Snippet[] snippets = bundle.findSnippetsFromPath(file.getLocation().toPortableString());
					
					for (Snippet snippet : snippets)
					{
						bundle.removeSnippet(snippet);
					}
				}
				else if (parentFolder.getName().equals(COMMANDS_FOLDER_NAME))
				{
					Command[] commands = bundle.findCommandsFromPath(file.getLocation().toPortableString());
					
					for (Command command : commands)
					{
						bundle.removeCommand(command);
					}
				}
			}
		}
	}
	
	/**
	 * showBundles
	 */
	public void showBundles()
	{
		if (this._bundles != null)
		{
			for (Bundle bundle : this._bundles)
			{
				System.out.println(bundle);
			}
		}
		else
		{
			System.out.println(Messages.BundleManager_NO_BUNDLES);
		}
	}
}
