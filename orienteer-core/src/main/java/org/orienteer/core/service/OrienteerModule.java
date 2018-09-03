package org.orienteer.core.service;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.server.OServer;
import de.agilecoders.wicket.webjars.settings.IWebjarsSettings;
import org.apache.commons.io.IOUtils;
import org.apache.wicket.Localizer;
import org.apache.wicket.extensions.markup.html.repeater.data.table.export.CSVDataExporter;
import org.apache.wicket.extensions.markup.html.repeater.data.table.export.IDataExporter;
import org.apache.wicket.protocol.http.WebApplication;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.service.impl.GuiceOrientDbSettings;
import org.orienteer.core.service.impl.OClassIntrospector;
import org.orienteer.core.service.impl.OrienteerWebjarsSettings;
import org.orienteer.core.tasks.OTaskManager;
import ru.ydn.wicket.wicketorientdb.DefaultODatabaseThreadLocalFactory;
import ru.ydn.wicket.wicketorientdb.IOrientDbSettings;

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * Main module to load Orienteer stuff to Guice
 * 
 * <h1>Properties</h1>
 * Properties can be retrieved from both files from the local filesystem and
 * files on the Java classpath. 
 * Highlevel lookup:
 * <ol>
 * <li>If there is a qualifier - lookup by this qualifier</li>
 * <li>If there is no a qualifier - lookup by default qualifier 'orienteer'</li>
 * <li>If nothing was found - use embedded configuration</li> 
 * </ol>
 * Order of lookup for a specific qualifier (for example 'myapplication'):
 * <ol>
 * <li>lookup of file specified by system property 'myapplication.properties'</li>
 * <li>lookup of URL specified by system property 'myapplication.properties'</li>
 * <li>lookup of file 'myapplication.properties' up from current directory</li>
 * <li>lookup of file 'myapplication.properties' in '~/orienteer/' directory</li>
 * <li>lookup of resource 'myapplication.properties' in a classpath</li>
 * </ol>
 */
public class OrienteerModule extends AbstractModule {

	public OrienteerModule() {
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void configure() {
		
		bind(IOrientDbSettings.class).to(GuiceOrientDbSettings.class).asEagerSingleton();
		bind(IOClassIntrospector.class).to(OClassIntrospector.class);
		bind(UIVisualizersRegistry.class).asEagerSingleton();
		bind(IWebjarsSettings.class).to(OrienteerWebjarsSettings.class).asEagerSingleton();
		bind(IDataExporter.class).to(CSVDataExporter.class);
	}

	@Provides
	@RequestScoped
	public ODatabaseDocumentTx getDatabaseDocumentTx(ODatabaseDocument db) {
		return (ODatabaseDocumentTx)db;
	}

	@Provides
	@RequestScoped
	public ODatabaseDocument getDatabaseRecord()
	{
		return DefaultODatabaseThreadLocalFactory.castToODatabaseDocument(ODatabaseRecordThreadLocal.instance().get().getDatabaseOwner());
	}

	@Provides
	@RequestScoped
	public OSchema getSchema(ODatabaseDocument db)
	{
		return db.getMetadata().getSchema();
	}

	@Provides
	public OServer getOServer(WebApplication application)
	{

		OrienteerWebApplication app = (OrienteerWebApplication)application;
		return app.getServer();
	}

	@Provides
	public Localizer getLocalizer(WebApplication application)
	{
		return application.getResourceSettings().getLocalizer();
	}
	
	@Provides
	public OTaskManager getTaskManager()
	{
		return OTaskManager.get();
	}

	@Provides
	@Named("orientdb.server.config")
	@Inject(optional = true)
	public String provideOrientDBConfig(@Named("orientdb.distributed") boolean distributed) {
		String config = readOrientDBConfigToString(distributed);
		config = config.replaceAll("\\$\\{root.password\\}", System.getProperty("root.password"));
		if (distributed) {
            config = config.replaceAll("\\$\\{configuration.db.default\\}", System.getProperty("configuration.db.default"));
            config = config.replaceAll("\\$\\{configuration.hazelcast\\}", System.getProperty("configuration.hazelcast"));
            config = config.replaceAll("\\$\\{node.name\\}", System.getProperty("node.name"));
            config = config.replaceAll("\\$\\{ip.address\\}", System.getProperty("ip.address"));
        }
        return config;
	}

	private String readOrientDBConfigToString(boolean distributed) {
		try {
			StringWriter writer = new StringWriter();
			InputStream in;
			if (distributed) {
				in = OrienteerWebApplication.class.getResource("distributed.db.config.xml").openStream();
			} else in = OrienteerWebApplication.class.getResource("db.config.xml").openStream();
			IOUtils.copy(in, writer, StandardCharsets.UTF_8);
			return writer.toString();
		} catch (Exception e) {
			// never return null, because one of configurations always is available in classpath
			return null;
		}
	}
}
