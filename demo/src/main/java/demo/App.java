package demo;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XPathQueryService;
import javax.xml.transform.Transformer;

public class App {

    private static final String URI = "xmldb:exist://localhost:8080/exist/xmlrpc/db/Tienda/productos";
    private static final String driver = "org.exist.xmldb.DatabaseImpl";

    @SuppressWarnings({ "rawtypes", "deprecation" })
    public static Collection getCollection() throws Exception {

        // Carga la clase del driver
        Class cl = Class.forName(driver);

        // Instancia el driver
        Database database = (Database) cl.newInstance();
        DatabaseManager.registerDatabase(database);

        // Obtiene la colección
        Collection col = DatabaseManager.getCollection(URI, "admin", "alvaro");
        return col;

    }

    public List<Producto> readResource(Collection col, String nomString) throws Exception {
        System.out.println("> Empezando leer <");
        List<Producto> productos = new ArrayList<>();
        // Obtiene el servicio XPath
        XPathQueryService xpath = (XPathQueryService) col.getService("XPathQueryService", "1.0");

        // Ejecuta una consulta XPath para obtener el recurso
        String expression = "/" + nomString + "/producto";
        ResourceSet result = xpath.query(expression);

        // Iterate through the result set and populate the Producto objects
        for (int i = 0; i < result.getSize(); i++) {
            Resource res = result.getResource(i);
            XMLResource xmlRes = (XMLResource) res;
            Producto producto = new Producto();
            Document doc = xmlRes.getContentAsDOM().getOwnerDocument();
            NodeList idNodes = doc.getElementsByTagName("id");
            producto.setId(idNodes.item(0).getTextContent());
            NodeList nombreNodes = doc.getElementsByTagName("nombre");
            producto.setNombre(nombreNodes.item(0).getTextContent());
            NodeList precioNodes = doc.getElementsByTagName("precio");
            producto.setPrecio(Double.parseDouble(precioNodes.item(0).getTextContent()));
            NodeList descripcionNodes = doc.getElementsByTagName("descripcion");
            producto.setDescripcion(descripcionNodes.item(0).getTextContent());
            NodeList cantidadNodes = doc.getElementsByTagName("cantidad");
            producto.setCantidad(Integer.parseInt(cantidadNodes.item(0).getTextContent()));
            productos.add(producto);
        }
        System.out.println("> terminado leer <");
        return productos;
    }

    public void writeResource(Collection col, String nomString, String xml) throws Exception {
        XMLResource res = (XMLResource) col.createResource(nomString, XMLResource.RESOURCE_TYPE);
        res.setContent(xml);
        col.storeResource(res);
    }

    public static String convertirProductoAXml(Producto producto) {
        try {
            // Crear un contexto JAXB para la clase Producto
            JAXBContext jaxbContext = JAXBContext.newInstance(Producto.class);
            // Crear un marshaller para convertir el objeto a XML
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); // Formatear la salida XML

            // Convertir el objeto Producto a XML
            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(producto, stringWriter);

            return stringWriter.toString();
        } catch (JAXBException e) {
            e.printStackTrace(); // Manejo de excepciones
            return null; // En caso de error, devolver null
        }
    }

    public void agregarProductoExist(Producto nuevoProducto) {
        try {
            String collectionUri = "xmldb:exist://localhost:8080/exist/xmlrpc/db/Tienda/productos";
            String xmlFileName = "productos.xml";

            // Obtener la colección y el documento XML existente
            Collection col = DatabaseManager.getCollection(collectionUri, "admin", "alvaro");
            XMLResource res = (XMLResource) col.getResource(xmlFileName);

            // Obtener el contenido del documento XML como un documento DOM
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader((String) res.getContent())));

            // Crear un nuevo elemento de producto
            Element nuevoElementoProducto = doc.createElement("producto");
            Element id = doc.createElement("id");
            id.appendChild(doc.createTextNode(String.valueOf(nuevoProducto.getId())));
            Element nombre = doc.createElement("nombre");
            nombre.appendChild(doc.createTextNode(nuevoProducto.getNombre()));
            Element precio = doc.createElement("precio");
            precio.appendChild(doc.createTextNode(String.valueOf(nuevoProducto.getPrecio())));
            Element descripcion = doc.createElement("descripcion");
            descripcion.appendChild(doc.createTextNode(nuevoProducto.getDescripcion()));
            Element cantidad = doc.createElement("cantidad");
            cantidad.appendChild(doc.createTextNode(String.valueOf(nuevoProducto.getCantidad())));

            // Agregar los elementos al nuevo elemento producto
            nuevoElementoProducto.appendChild(id);
            nuevoElementoProducto.appendChild(nombre);
            nuevoElementoProducto.appendChild(precio);
            nuevoElementoProducto.appendChild(descripcion);
            nuevoElementoProducto.appendChild(cantidad);

            // Agregar el nuevo elemento producto al documento existente
            doc.getDocumentElement().appendChild(nuevoElementoProducto);

            // Guardar el documento actualizado en la base de datos
            StringWriter stringWriter = new StringWriter();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(doc), new StreamResult(stringWriter));
            res.setContent(stringWriter.toString());
            col.storeResource(res);
        } catch (Exception e) {
            e.printStackTrace(); // Manejo de excepciones
        }
    }
    public int obtenerUltimaId() {
        int ultimaId = 0;
        try {
            String collectionUri = "xmldb:exist://localhost:8080/exist/xmlrpc/db/Tienda/productos";
            String xmlFileName = "productos.xml";
    
            // Obtener la colección y el documento XML existente
            Collection col = DatabaseManager.getCollection(collectionUri, "admin", "alvaro");
            XMLResource res = (XMLResource) col.getResource(xmlFileName);
    
            // Obtener el contenido del documento XML como un documento DOM
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader((String) res.getContent())));
    
            // Obtener la lista de nodos de id
            NodeList idNodes = doc.getElementsByTagName("id");
    
            // Recorrer la lista de nodos de id para encontrar el valor más grande
            for (int i = 0; i < idNodes.getLength(); i++) {
                int id = Integer.parseInt(idNodes.item(i).getTextContent());
                if (id > ultimaId) {
                    ultimaId = id;
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Manejo de excepciones
        }
        return ultimaId;
    }

    public void eliminarProductoPorId(int id) {
        try {
            String collectionUri = "xmldb:exist://localhost:8080/exist/xmlrpc/db/Tienda/productos";
            String xmlFileName = "productos.xml";
    
            // Obtener la colección y el documento XML existente
            Collection col = DatabaseManager.getCollection(collectionUri, "admin", "alvaro");
            XMLResource res = (XMLResource) col.getResource(xmlFileName);
    
            // Obtener el contenido del documento XML como un documento DOM
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader((String) res.getContent())));
            boolean encontrar = false;
            // Encontrar el elemento producto con el id proporcionado y eliminarlo
            NodeList productos = doc.getElementsByTagName("producto");
            for (int i = 0; i < productos.getLength(); i++) {
                Element producto = (Element) productos.item(i);
                int productId = Integer.parseInt(producto.getElementsByTagName("id").item(0).getTextContent());
                if (productId == id) {
                    encontrar = true;
                    producto.getParentNode().removeChild(producto);
                }
            }
            if (encontrar) {
                
            
            // Guardar el documento actualizado en la base de datos
            StringWriter stringWriter = new StringWriter();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(doc), new StreamResult(stringWriter));
            res.setContent(stringWriter.toString());
            col.storeResource(res);
            }else{
                System.out.println("No se encontro el producto con el id proporcionado");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Manejo de excepciones
        }
    }

    public void editarProductoPorId(int id, Producto productoEditado) {
        try {
            String collectionUri = "xmldb:exist://localhost:8080/exist/xmlrpc/db/Tienda/productos";
            String xmlFileName = "productos.xml";
    
            // Obtener la colección y el documento XML existente
            Collection col = DatabaseManager.getCollection(collectionUri, "admin", "alvaro");
            XMLResource res = (XMLResource) col.getResource(xmlFileName);
    
            // Obtener el contenido del documento XML como un documento DOM
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader((String) res.getContent())));
            boolean encontrar = false;
            // Encontrar el elemento producto con el id proporcionado y actualizar sus campos
            NodeList productos = doc.getElementsByTagName("producto");
            for (int i = 0; i < productos.getLength(); i++) {
                Element producto = (Element) productos.item(i);
                int productId = Integer.parseInt(producto.getElementsByTagName("id").item(0).getTextContent());
                System.out.println(productId);
                if (productId == id) {
                    encontrar = true;
                    producto.getElementsByTagName("nombre").item(0).setTextContent(productoEditado.getNombre());
                    producto.getElementsByTagName("precio").item(0).setTextContent(String.valueOf(productoEditado.getPrecio()));
                    producto.getElementsByTagName("descripcion").item(0).setTextContent(productoEditado.getDescripcion());
                    producto.getElementsByTagName("cantidad").item(0).setTextContent(String.valueOf(productoEditado.getCantidad()));
                }
            }
            if (encontrar) {
            StringWriter stringWriter = new StringWriter();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(doc), new StreamResult(stringWriter));
            res.setContent(stringWriter.toString());
            col.storeResource(res);
            }else{
                System.out.println("No se encontro el id");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Manejo de excepciones
        }
    }
}