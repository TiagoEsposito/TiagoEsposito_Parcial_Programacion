/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controllers;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import modelo.Carrito; 
import modelo.Producto;

/**
 *
 * @author lukit
 */
public class VentaController implements Initializable {
    
    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TextField txtCantidad;
    @FXML private ListView<String> listaCarrito; 
    
    private ObservableList<Producto> listaInventario;
    
  
    private ArrayList<Carrito> carritoLogico;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        listaInventario = FXCollections.observableArrayList();
        carritoLogico = new ArrayList<>();
        
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        
        tablaProductos.setItems(listaInventario);
        cargarProductos();
    }

    private void cargarProductos() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("productos.dat"))) {
            ArrayList<Producto> datos = (ArrayList<Producto>) ois.readObject();
            listaInventario.addAll(datos);
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error al cargar productos: " + e.getMessage()).show();
        }
    }

        
    @FXML
    private void agregarAlCarrito(ActionEvent event) {
       Producto productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();
       
       if (productoSeleccionado == null) {
           mostrarAlerta(Alert.AlertType.WARNING, "Debe seleccionar un producto.");
           return;
       }
       
       int cantidad;
       try {
           cantidad = Integer.parseInt(txtCantidad.getText());
       } catch (NumberFormatException e) {
           mostrarAlerta(Alert.AlertType.WARNING, "La cantidad debe ser un número entero.");
           return;
       }
       
       if (cantidad <= 0) {
           mostrarAlerta(Alert.AlertType.WARNING, "La cantidad debe ser mayor a 0.");
           return;
       }
       
       if (cantidad > productoSeleccionado.getStock()) {
           mostrarAlerta(Alert.AlertType.WARNING, "No hay stock suficiente. Disponible: " + productoSeleccionado.getStock());
           return;
       }
       
    
       productoSeleccionado.setStock(productoSeleccionado.getStock() - cantidad);
       tablaProductos.refresh();
       

       listaCarrito.getItems().add(productoSeleccionado.getNombre() + " (" + cantidad + ")");
       

       carritoLogico.add(new Carrito(productoSeleccionado, cantidad));
       
       txtCantidad.setText("");
    }

    @FXML
    private void confirmarCompra(ActionEvent event) {
        if (carritoLogico.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "El carrito está vacío.");
            return;
        }
        
        double totalPagar = 0;
        
        try (PrintWriter writer = new PrintWriter("ticket.txt")) {
            writer.println("=== TICKET DE COMPRA ===");
            writer.println("Nombre producto       Cantidad       Subtotal");
            writer.println("--------------------------------------------");
       
            for (Carrito item : carritoLogico) {
                double subtotal = item.getProducto().getPrecio() * item.getCantidad();
                totalPagar += subtotal;

                writer.println(item.getProducto().getNombre() + "    " + item.getCantidad() + "      $" + subtotal);
            }

            writer.println("--------------------------------------");
            writer.println("TOTAL A PAGAR: $" + totalPagar);
            
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al generar el ticket.");
            return;
        }
        
        guardarCambiosEnDat();

        carritoLogico.clear();
        listaCarrito.getItems().clear();
        
        mostrarAlerta(Alert.AlertType.INFORMATION, "Compra exitosa. Ticket generado.");
    }
    
    private void guardarCambiosEnDat() {
        ArrayList<Producto> listaAGuardar = new ArrayList<>(listaInventario);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("productos.dat"))) {
            oos.writeObject(listaAGuardar);
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al guardar el stock.");
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setContentText(mensaje);
        alert.show();
    }
}