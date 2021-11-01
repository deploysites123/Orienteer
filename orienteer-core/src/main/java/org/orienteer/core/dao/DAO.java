package org.orienteer.core.dao;

import org.apache.wicket.util.lang.Args;
import org.orienteer.transponder.Transponder;
import org.orienteer.transponder.orientdb.IODocumentWrapper;
import org.orienteer.transponder.orientdb.ODriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.type.ODocumentWrapper;

/**
 * Utility class for creating implementations for required interfaces
 * @deprecated Use Transponder directly
 */
@Deprecated
public final class DAO {
	
	private static final Logger LOG = LoggerFactory.getLogger(DAO.class);
	public static final Transponder TRANSPONDER = new Transponder(new OrienteerDriver(true));
	
	private DAO() {
		
	}
	
	public static IODocumentWrapper asWrapper(Object obj) {
		if(obj==null) return null;
		else if (obj instanceof IODocumentWrapper) return (IODocumentWrapper)obj;
		else throw new IllegalStateException("Object is not a wrapper. Object: "+obj);
	}
	
	public static ODocument asDocument(Object obj) {
		return obj!=null?asWrapper(obj).getDocument():null;
	}
	
	public static <T> T as(Object proxy, Class<? extends T> clazz) {
		return clazz.cast(proxy);
	}
	
	public static <T> T loadFromDocument(T obj, ODocument doc) {
		asWrapper(obj).fromStream(doc);
		return obj;
	}
	
	public static <T> T save(T obj) {
		return asWrapper(obj).save();
	}
	
	public static <T> T reload(T obj) {
		return asWrapper(obj).reload();
	}
	
	public static <T> T create(Class<T> interfaceClass, Class<?>... additionalInterfaces) {
		return TRANSPONDER.create(interfaceClass, additionalInterfaces);
	}
	
	public static <T> T create(Class<T> interfaceClass, String className, Class<?>... additionalInterfaces) {
		return TRANSPONDER.create(interfaceClass, className, additionalInterfaces);
	}
	
	public static <T> T provide(Class<T> interfaceClass, ORID iRID, Class<?>... additionalInterfaces) {
		if(iRID==null) throw new NullPointerException("ORID for DAO.provide(...) should not be null");
		return provide(interfaceClass, new ODocumentWrapper(iRID), additionalInterfaces);
	}
	
	public static <T> T provide(Class<T> interfaceClass, ODocument doc, Class<?>... additionalInterfaces) {
		if(doc==null) throw new NullPointerException("Document for DAO.provide(...) should not be null");
		return provide(interfaceClass, new ODocumentWrapper(doc), additionalInterfaces);
	}
	
	public static <T> T provide(Class<T> interfaceClass, OIdentifiable id, Class<?>... additionalInterfaces) {
		if(id instanceof ODocument) return provide(interfaceClass, (ODocument) id, additionalInterfaces);
		else return provide(interfaceClass, id.getIdentity(), additionalInterfaces);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T provide(Class<? extends T> interfaceClass, ODocumentWrapper docWrapper, Class<?>... additionalInterfaces) {
		Args.notNull(ODatabaseRecordThreadLocal.instance().get(), "There is no DatabaseSession");
		return TRANSPONDER.provide(docWrapper, interfaceClass, additionalInterfaces);
	}
	
	/**public static <T> T updateBy(T proxy, Class<?>... additionalInterfaces) {
		if(additionalInterfaces==null 
				|| additionalInterfaces.length==0 
				|| compatible(proxy, additionalInterfaces)) return (T) proxy;
		else {
			ClassLoader classLoader = additionalInterfaces[0].getClassLoader();
			InvocationHandler invocationHandler = Proxy.getInvocationHandler(proxy);
			Set<Class<?>> interfaces = new HashSet<Class<?>>();
			interfaces.addAll(Arrays.asList(additionalInterfaces));
			interfaces.addAll(Arrays.asList(proxy.getClass().getInterfaces()));
			return (T) Proxy.newProxyInstance(classLoader, interfaces.toArray(new Class[interfaces.size()]),  invocationHandler);
		}
	}**/
	
	/**public static <T> T upgradeTo(Object proxy, Class<? extends T> interfaceClass, Class<?>... additionalInterfaces) {
		if(compatible(proxy, interfaceClass) && compatible(proxy, additionalInterfaces)) return (T) proxy;
		else {
			ClassLoader classLoader = interfaceClass.getClassLoader();
			InvocationHandler invocationHandler = Proxy.getInvocationHandler(proxy);
			Set<Class<?>> interfaces = new HashSet<Class<?>>();
			interfaces.add(interfaceClass);
			interfaces.addAll(Arrays.asList(additionalInterfaces));
			interfaces.addAll(Arrays.asList(proxy.getClass().getInterfaces()));
			return (T) Proxy.newProxyInstance(classLoader, interfaces.toArray(new Class[interfaces.size()]),  invocationHandler);
		}
	}**/
	
	public static boolean compatible(Object proxy, Class<?>... interfaces) {
		for (Class<?> inter : interfaces) {
			if(!inter.isInstance(proxy)) return false;
		}
		return true;
	}
	
	/*private static <T> Class<? extends T> tryToGetInheritedInterface(Class<? extends T> clazz, ODocumentWrapper docWrapper) {
		ODocument doc = docWrapper.getDocument();
		if(doc!=null) {
			String daoClassName = CustomAttribute.DAO_CLASS.getValue(doc.getSchemaClass());
			if(daoClassName!=null) {
				try {
					Class<? extends T> daoClass = (Class<? extends T>)Class.forName(daoClassName);
					if(clazz.isAssignableFrom(daoClass)) return daoClass;
				} catch (ClassNotFoundException e) {
					//NOP
				}
			}
		}
		return clazz;
	}*/
	
	@SuppressWarnings("unchecked")
	public static <T> T dao(Class<T> interfaceClass, Class<?>... additionalInterfaces) {
		return TRANSPONDER.dao(interfaceClass, additionalInterfaces);
	}
	
	public static void define(Class<?>...classes) {
		TRANSPONDER.define(classes);
	}
	
}