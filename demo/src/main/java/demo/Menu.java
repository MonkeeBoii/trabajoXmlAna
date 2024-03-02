package demo;

import java.util.List;
import java.util.Scanner;

import org.xmldb.api.base.Collection;

public class Menu {
    String seleccion;
    App app = new App();
    Scanner scanner = new Scanner(System.in);
    Collection collection;
    boolean salir = false;
    List<Producto> productos;

    public Menu() {
        MostrarMenu();
    }

    public void Start() {
        while (!salir) {
            String entrada = scanner.nextLine();
            try {
                collection = App.getCollection();
                accion(entrada);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void MostrarMenu() {
        System.out.println("-------------------------------------");
        System.out.println("#                                   #");
        System.out.println("#   leer                            #");
        System.out.println("#   newProducto                     #");
        System.out.println("#   eliminar                        #");
        System.out.println("#   editar                          #");
        System.out.println("#                                   #");
        System.out.println("-------------------------------------");
    }

    public void accion(String entrada) throws Exception {
        switch (entrada) {
            case "leer":
                productos = app.readResource(collection, "productos");
                break;
            case "new":
                Producto nuevoProducto = new Producto();
                System.out.println("Introduce el nombre");
                nuevoProducto.setNombre(scanner.nextLine());
                System.out.println("Introduce el precio");
                nuevoProducto.setPrecio(Double.parseDouble(scanner.nextLine()));
                System.out.println("Introduce la descripcio");
                nuevoProducto.setDescripcion(scanner.nextLine());
                System.out.println("Introduce la cantidad");
                nuevoProducto.setCantidad(Integer.parseInt(scanner.nextLine()));
                nuevoProducto.setId(String.valueOf(app.obtenerUltimaId() + 1));
                app.agregarProductoExist(nuevoProducto);
                break;
            case "eliminar":
                System.out.println("Introduce el id");
                app.eliminarProductoPorId(Integer.parseInt(scanner.nextLine()));
                break;
            case "editar":
                Producto newProducto = new Producto();
                System.out.println("Introduce el id");
                newProducto.setId(scanner.nextLine());
                System.out.println("Introduce el nombre");
                newProducto.setNombre(scanner.nextLine());
                System.out.println("Introduce el precio");
                newProducto.setPrecio(Double.parseDouble(scanner.nextLine()));
                System.out.println("Introduce la descripcio");
                newProducto.setDescripcion(scanner.nextLine());
                System.out.println("Introduce la cantidad");
                newProducto.setCantidad(Integer.parseInt(scanner.nextLine()));
                app.editarProductoPorId(Integer.parseInt(newProducto.getId()), newProducto);
                System.out.println("Editado");
                break;
            case "leerArray":
                for (Producto producto : productos) {
                    System.out.println(producto.toString());
                }
                break;
            case "exit":
                salir = true;
                break;
            default:
                break;
        }
    }
}
