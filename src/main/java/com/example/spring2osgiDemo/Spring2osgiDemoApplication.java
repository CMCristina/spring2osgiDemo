package com.example.spring2osgiDemo;

import java.io.File;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@SpringBootApplication
@Controller
@EnableAutoConfiguration
public class Spring2osgiDemoApplication {

	private static String[] libs = null;
	BundleContext context;

	public static void main(String[] args) {
		SpringApplication.run(Spring2osgiDemoApplication.class, args);
	}

	@RequestMapping("/")
	@ResponseBody
	String home() {
		startOsgiFramework();
		installOsgiFrameworkBundles();
		installCustomOsgiFrameworkBundles();

		return "Hello World from Home";
	}

	protected void startOsgiFramework() {

		System.out.println("Start Osgi Framework.");
		FrameworkFactory frameworkFactory = ServiceLoader.load(FrameworkFactory.class).iterator().next();

		Map<String, String> config = new HashMap<String, String>();
		config.put("osgi.console", "");
		config.put("osgi.clean", "true");
		config.put("osgi.noShutdown", "true");
		config.put("eclipse.ignoreApp", "true");
		config.put("osgi.bundles.defaultStartLevel", "4");
		config.put("osgi.configuration.area", ".\\lib\\plugins\\configuration");

		// automated bundles deployment
		// config.put("felix.fileinstall.dir", "./dropins");
		config.put("felix.fileinstall.noInitialDelay", "true");
		config.put("felix.fileinstall.start.level", "4");

		Framework framework = frameworkFactory.newFramework(config);

		try {
			framework.start();
			context = framework.getBundleContext();
			System.out.println("Osgi Framework started.");
		} catch (BundleException e) {
			e.printStackTrace();
		}
	}

	protected void installOsgiFrameworkBundles() {
		String found = null;

		System.out.println("Install Osgi Framework Bundles.");
		for (String jar : getLibs()) {
			if (jar.endsWith(".jar")) {

				found = String.format("file:.\\lib\\plugins\\%s", jar);

				try {
					if (context.getBundle(found) == null) {
						context.installBundle(found);
					}

				} catch (BundleException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("Osgi Framework Bundels installed.");

	}

	protected void installCustomOsgiFrameworkBundles() {
		try {

			context.installBundle("file:.\\lib\\customPlugins\\osgi.helloWorld-1.0.0-SNAPSHOT.jar").start();
			context.installBundle("file:.\\customPlugins\\absHelloWorldPlugin-1.0.0-SNAPSHOT.jar").start();

			GregorianCalendar gcal = new GregorianCalendar();
			XMLGregorianCalendar xgcal;
			try {
				xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
				System.out.println("Hello from spring2osgiDemoApp " + xgcal);
			} catch (DatatypeConfigurationException e) {
				e.printStackTrace();
			}

		} catch (BundleException e1) {
			e1.printStackTrace();
		}

	}

	private String[] getLibs() {
		if (libs == null) {
			// List<String> jarsList = new ArrayList<String>();
			HashSet<String> jarSet = new HashSet<String>();
			File pluginsDir = new File(".\\lib\\plugins");
			for (String jar : pluginsDir.list()) {
				jarSet.add(jar);
			}
			libs = jarSet.toArray(new String[jarSet.size()]);
		}
		return libs;
	}

}
