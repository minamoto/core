package org.jboss.webbeans.xml;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.inject.DefinitionException;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.resources.spi.ResourceLoadingException;

public class ParseXmlHelper
{   
   public static boolean isJavaEeNamespace(Element element)
   {
      return element.getNamespace().getURI().equalsIgnoreCase(XmlConstants.JAVA_EE_NAMESPACE);
   }
   
   public static <T> AnnotatedClass<? extends T> loadElementClass(Element element, Class<T> expectedType, XmlEnvironment environment, Map<String, Set<String>> packagesMap)
   {
      List<AnnotatedClass<? extends T>> classesList = new ArrayList<AnnotatedClass<? extends T>>();
      String className = element.getName();
      String prefix = element.getNamespacePrefix();
      
      for(Map.Entry<String, Set<String>> packagesEntry : packagesMap.entrySet())
      {
         if(prefix.equalsIgnoreCase(packagesEntry.getKey()))
         {
            Set<String> packages = packagesEntry.getValue();
            for(String packageName : packages)
            {
               String classPath = packageName + "." + element.getName();
               try
               {
                  AnnotatedClass<? extends T> classType = environment.loadClass(classPath, expectedType);
                  classesList.add(classType);
               }
               catch(ResourceLoadingException e){}
            }
         }
      }
      
      if(classesList.size() == 0)
         throw new DefinitionException("Could not find '" + className + "'");
      
      if(classesList.size() == 1)
         return classesList.get(0);
      
      throw new DefinitionException("There are multiple packages containing a Java type with the same name '" + className + "'");
   }
   
   public static void checkRootAttributes(Element root, Map<String, Set<String>> packagesMap)
   {
      Iterator<?> rootAttrIterator = root.attributeIterator();
      while(rootAttrIterator.hasNext())
      {
         Set<String> packagesSet = new HashSet<String>();
         Attribute attribute = (Attribute)rootAttrIterator.next();
         String attrPrefix = attribute.getNamespacePrefix();         
         String attrData = attribute.getStringValue();
         
         String urn = "";
         for(String attrVal : attrData.split(" "))
         {
            if(attrVal.startsWith(XmlConstants.URN_PREFIX))
            {
               urn = attrVal;
               URL namespaceFile = loadFile(urn, XmlConstants.NAMESPACE_FILE_NAME);
               if(namespaceFile == null)
                  throw new DefinitionException("Could not find '" + XmlConstants.NAMESPACE_FILE_NAME + "' file according to specified URN '" + urn + "'");
               packagesSet.addAll(parseNamespaceFile(namespaceFile));
            }
            if(attribute.getName().equalsIgnoreCase(XmlConstants.SCHEMA_LOCATION) && 
                  attrVal.startsWith(XmlConstants.HTTP_PREFIX) && urn.trim().length() > 0)
            {
               URL schemaFile = loadFile(urn, XmlConstants.SCHEMA_FILE_NAME);
               if(schemaFile == null)
                  throw new DefinitionException("Could not find '" + XmlConstants.SCHEMA_FILE_NAME + "' file according to specified URN '" + urn + "'");
            }
         }
         
         addElementToPackagesMap(packagesMap, attrPrefix, packagesSet);
      }
   }
   
   public static void checkRootDeclaredNamespaces(Element root, Map<String, Set<String>> packagesMap)
   {
      Iterator<?> namespacesIterator = root.declaredNamespaces().iterator();
      while(namespacesIterator.hasNext())
      {
         Namespace namespace = (Namespace)namespacesIterator.next();
         String prefix = namespace.getPrefix();
         String uri = namespace.getURI();
         if(uri.startsWith(XmlConstants.URN_PREFIX))
         {
            Set<String> packagesSet = new HashSet<String>();
            
            URL namespaceFile = loadFile(uri, XmlConstants.NAMESPACE_FILE_NAME);
            if(namespaceFile != null)
            {
               packagesSet.addAll(parseNamespaceFile(namespaceFile));
            }
            else
            {
               String packageName = uri.replaceFirst(XmlConstants.URN_PREFIX, "");
               packagesSet.add(packageName);
            }            
            
            addElementToPackagesMap(packagesMap, prefix, packagesSet);
         }
      }
   }
   
   private static URL loadFile(String urn, String fileName)
   {
      char separator = '/';
      String packageName = urn.replaceFirst(XmlConstants.URN_PREFIX, "");
      String path = packageName.replace('.', separator);
      String filePath = separator + path + separator + fileName;
      URL namespaceFile = ParseXmlHelper.class.getResource(filePath);      
      return namespaceFile;
   }
   
   private static Set<String> parseNamespaceFile(URL namespaceFile)
   {
      Set<String> packages = new HashSet<String>();
      Scanner fileScanner;
      try
      {
         fileScanner = new Scanner(namespaceFile.openStream());
         while (fileScanner.hasNextLine() )
         {
            String line = fileScanner.nextLine();
            Scanner lineScanner = new Scanner(line);
            lineScanner.useDelimiter(XmlConstants.NAMESPACE_FILE_DELIMETER);
            while(lineScanner.hasNext())
            {
               packages.add(lineScanner.next());
            }
            lineScanner.close();
         }
         fileScanner.close();
         return packages;
      }
      catch (IOException e)
      {
         throw new RuntimeException("Error opening " + namespaceFile.toString());
      }         
   }
   
   private static void addElementToPackagesMap(Map<String, Set<String>> packagesMap, String prefix, Set<String> packagesSet)
   {
      if(packagesMap.containsKey(prefix))
      {
         Set<String> packages = packagesMap.get(prefix);
         packages.addAll(packagesSet);
         packagesMap.put(prefix, packages);
      }
      else
      {
         packagesMap.put(prefix, packagesSet);
      }
   }
}
