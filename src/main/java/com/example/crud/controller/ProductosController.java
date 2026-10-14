
package com.example.crud.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import com.example.crud.database.DatabaseConnection;
import com.example.crud.model.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductosController {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtDescripcion;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtStock;

    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<String> cmbMarca;

    @FXML private DatePicker dtpFechaCreacion;
    @FXML private TextField txtBuscarCodigoNombre;
    @FXML private TextField txtprecioMinimo;
    @FXML private TextField txtPrecioMaximo;

    @FXML private DatePicker dtpDesde;
    @FXML private DatePicker dtpHasta;

    @FXML private ComboBox<String> cmbCategoriaBusqueda;
    @FXML private ComboBox<String> cmbOrdenar;

    @FXML private CheckBox chkExistencia;
    @FXML private Button btnBuscar;
    @FXML private Button btnGuardar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnActualizar;
    @FXML private Button btnActualizarRegistro;
    @FXML private Button btnEliminar;

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, String> colMarca;
    @FXML private TableColumn<Producto, java.time.LocalDate> colFecha;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;

    private final ObservableList<Producto> productos =
            FXCollections.observableArrayList();


    @FXML
    private void initialize() {
        configurarComboboxCategoria();
        configurarComboboxMarca();
        configurarTableView();

        tblProductos.setItems(productos);

        tblProductos.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, seleccionado) -> {
                    if (seleccionado != null) {
                        llenarFormulario(seleccionado);
                    }
                });

        actualizarTabla();
        cmbCategoriaBusqueda.getItems().clear();
        cmbCategoriaBusqueda.getItems()
                .addAll(cmbCategoria.getItems());

        cmbOrdenar.getItems().addAll(
                "Nombre A-Z",
                "Nombre Z-A",
                "Precio ascendente",
                "Precio descendente",
                "Mayor stock",
                "Menor stock",
                "Fecha más reciente",
                "Fecha más antigua"
        );
    }


    // Cargar categorías desde PostgreSQL
    private void configurarComboboxCategoria() {
        cmbCategoria.getItems().clear();

        String sql = "SELECT nombre FROM catalogos.categorias "
                + "WHERE estado = 1 ORDER BY nombre ASC";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                cmbCategoria.getItems().add(
                        resultSet.getString("nombre"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "No se pudieron cargar las categorías",
                    ex.getMessage());
        }
    }


    // Cargar marcas desde PostgreSQL
    private void configurarComboboxMarca() {
        cmbMarca.getItems().clear();

        String sql = "SELECT nombre FROM catalogos.marcas "
                + "WHERE estado = 1 ORDER BY nombre ASC";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                cmbMarca.getItems().add(
                        resultSet.getString("nombre"));
            }

            System.out.println(
                    "Marcas cargadas: " + cmbMarca.getItems());

        } catch (SQLException ex) {
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "No se pudieron cargar las marcas",
                    ex.getMessage());
        }
    }


    // Configurar columnas de la tabla
    private void configurarTableView() {
        colId.setCellValueFactory(
                new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(
                new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(
                new PropertyValueFactory<>("nombreCategoria"));
        colMarca.setCellValueFactory(
                new PropertyValueFactory<>("nombreMarca"));
        colFecha.setCellValueFactory(
                new PropertyValueFactory<>("fechaCreacion"));
        colPrecio.setCellValueFactory(
                new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(
                new PropertyValueFactory<>("stock"));
    }


    // Guardar producto
    @FXML
    private void guardar() {
        if (!validarCampos()) {
            return;
        }

        String sql = "INSERT INTO catalogos.productos "
                + "(codigo, nombre, descripcion, precio, stock, "
                + "fecha_creacion, id_categoria, id_marca, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, "
                + "(SELECT id_categoria FROM catalogos.categorias "
                + "WHERE nombre = ?), "
                + "(SELECT id_marca FROM catalogos.marcas "
                + "WHERE nombre = ?), 1)";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, txtCodigo.getText().trim());
            statement.setString(2, txtNombre.getText().trim());
            statement.setString(3, txtDescripcion.getText().trim());
            statement.setDouble(4,
                    Double.parseDouble(txtPrecio.getText().trim()));
            statement.setInt(5,
                    Integer.parseInt(txtStock.getText().trim()));
            statement.setDate(6,
                    java.sql.Date.valueOf(dtpFechaCreacion.getValue()));
            statement.setString(7, cmbCategoria.getValue());
            statement.setString(8, cmbMarca.getValue());

            statement.executeUpdate();

            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito",
                    "Producto guardado",
                    "El producto se almacenó exitosamente.");

            limpiar(null);
            actualizarTabla();

        } catch (SQLException ex) {
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "No se pudo guardar", ex.getMessage());
        }
    }


    // Actualizar producto seleccionado
    @FXML
    private void actualizarRegistro() {
        Producto seleccionado =
                tblProductos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atención",
                    "Sin selección",
                    "Selecciona un producto de la tabla primero.");
            return;
        }

        if (!validarCampos()) {
            return;
        }

        String sql = "UPDATE catalogos.productos SET "
                + "codigo = ?, nombre = ?, descripcion = ?, "
                + "precio = ?, stock = ?, fecha_creacion = ?, "
                + "id_categoria = (SELECT id_categoria "
                + "FROM catalogos.categorias WHERE nombre = ?), "
                + "id_marca = (SELECT id_marca "
                + "FROM catalogos.marcas WHERE nombre = ?) "
                + "WHERE id_producto = ? AND estado = 1";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, txtCodigo.getText().trim());
            statement.setString(2, txtNombre.getText().trim());
            statement.setString(3, txtDescripcion.getText().trim());
            statement.setDouble(4,
                    Double.parseDouble(txtPrecio.getText().trim()));
            statement.setInt(5,
                    Integer.parseInt(txtStock.getText().trim()));
            statement.setDate(6,
                    java.sql.Date.valueOf(dtpFechaCreacion.getValue()));
            statement.setString(7, cmbCategoria.getValue());
            statement.setString(8, cmbMarca.getValue());
            statement.setInt(9, seleccionado.getIdProducto());

            statement.executeUpdate();

            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito",
                    "Producto actualizado",
                    "El producto se actualizó correctamente.");

            limpiar(null);
            actualizarTabla();

        } catch (SQLException ex) {
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "No se pudo actualizar", ex.getMessage());
        }
    }


    // Eliminación lógica del producto
    @FXML
    private void eliminar() {
        Producto seleccionado =
                tblProductos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atención",
                    "Sin selección",
                    "Selecciona un producto de la tabla primero.");
            return;
        }

        Alert confirmacion =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText("¿Eliminar este producto?");
        confirmacion.setContentText(seleccionado.getNombre());

        if (confirmacion.showAndWait()
                .orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        String sql = "UPDATE catalogos.productos "
                + "SET estado = 0 "
                + "WHERE id_producto = ? AND estado = 1";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, seleccionado.getIdProducto());
            statement.executeUpdate();

            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito",
                    "Producto eliminado",
                    "El producto se desactivó correctamente.");

            limpiar(null);
            actualizarTabla();

        } catch (SQLException ex) {
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "No se pudo eliminar", ex.getMessage());
        }
    }


    // Cargar los datos del producto seleccionado en el formulario
    private void llenarFormulario(Producto p) {
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        txtDescripcion.setText(p.getDescripcion());
        txtPrecio.setText(String.valueOf(p.getPrecio()));
        txtStock.setText(String.valueOf(p.getStock()));

        cmbCategoria.setValue(p.getNombreCategoria());
        cmbMarca.setValue(p.getNombreMarca());

        dtpFechaCreacion.setValue(p.getFechaCreacion());
    }


    // Actualizar la tabla
    @FXML
    private void actualizarTabla() {
        String sql = "SELECT p.id_producto, p.codigo, p.nombre, "
                + "p.descripcion, p.precio, p.stock, "
                + "p.fecha_creacion, p.id_categoria, p.id_marca, "
                + "p.estado, c.nombre AS nombre_categoria, "
                + "m.nombre AS nombre_marca "
                + "FROM catalogos.productos p "
                + "INNER JOIN catalogos.categorias c "
                + "ON p.id_categoria = c.id_categoria "
                + "INNER JOIN catalogos.marcas m "
                + "ON p.id_marca = m.id_marca "
                + "WHERE p.estado = 1 "
                + "ORDER BY p.id_producto ASC";

        cargarProductos(sql);
    }


    // Ejecutar un SELECT y mostrar el resultado en el TableView
    private void cargarProductos(String sql) {
        productos.clear();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                Producto producto = new Producto();

                producto.setIdProducto(
                        resultSet.getInt("id_producto"));
                producto.setCodigo(
                        resultSet.getString("codigo"));
                producto.setNombre(
                        resultSet.getString("nombre"));
                producto.setDescripcion(
                        resultSet.getString("descripcion"));
                producto.setPrecio(
                        resultSet.getDouble("precio"));
                producto.setStock(
                        resultSet.getInt("stock"));
                producto.setFechaCreacion(
                        resultSet.getDate("fecha_creacion")
                                .toLocalDate());
                producto.setIdCategoria(
                        resultSet.getInt("id_categoria"));
                producto.setIdMarca(
                        resultSet.getInt("id_marca"));
                producto.setEstado(
                        resultSet.getInt("estado") == 1);
                producto.setNombreCategoria(
                        resultSet.getString("nombre_categoria"));
                producto.setNombreMarca(
                        resultSet.getString("nombre_marca"));

                productos.add(producto);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "No se pudo consultar", ex.getMessage());
        }
    }


    // Validar los campos obligatorios
    private boolean validarCampos() {
        if (txtCodigo.getText().isBlank()
                || txtNombre.getText().isBlank()
                || txtDescripcion.getText().isBlank()
                || txtPrecio.getText().isBlank()
                || txtStock.getText().isBlank()
                || cmbCategoria.getValue() == null
                || cmbMarca.getValue() == null
                || dtpFechaCreacion.getValue() == null) {

            mostrarAlerta(Alert.AlertType.WARNING, "Validación",
                    "Campos incompletos",
                    "Completa todos los campos obligatorios.");

            return false;
        }

        try {
            double precio =
                    Double.parseDouble(txtPrecio.getText().trim());

            int stock =
                    Integer.parseInt(txtStock.getText().trim());

            if (precio <= 0 || stock < 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validación",
                        "Valores inválidos",
                        "El precio debe ser mayor que cero y el stock no puede ser negativo.");
                return false;
            }

        } catch (NumberFormatException ex) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación",
                    "Valores inválidos",
                    "El precio debe ser numérico y el stock debe ser entero.");
            return false;
        }

        return true;
    }


    // Limpiar el formulario
    @FXML
    public void limpiar(ActionEvent actionEvent) {
        txtCodigo.clear();
        txtNombre.clear();
        txtDescripcion.clear();
        txtPrecio.clear();
        txtStock.clear();

        cmbCategoria.getSelectionModel().clearSelection();
        cmbMarca.getSelectionModel().clearSelection();

        dtpFechaCreacion.setValue(null);

        tblProductos.getSelectionModel().clearSelection();
    }


    // Mostrar mensajes
    private void mostrarAlerta(
            Alert.AlertType tipo, String titulo,
            String encabezado, String mensaje) {

        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    @FXML
    private void buscarProductos() {
        StringBuilder sql = new StringBuilder(
                "SELECT p.id_producto, p.codigo, p.nombre, " +
                        "p.descripcion, p.precio, p.stock, " +
                        "p.fecha_creacion, p.id_categoria, p.id_marca, " +
                        "p.estado, c.nombre AS nombre_categoria, " +
                        "m.nombre AS nombre_marca " +
                        "FROM catalogos.productos p " +
                        "INNER JOIN catalogos.categorias c " +
                        "ON p.id_categoria = c.id_categoria " +
                        "INNER JOIN catalogos.marcas m " +
                        "ON p.id_marca = m.id_marca " +
                        "WHERE p.estado = 1"
        );

        ObservableList<Object> parametros =
                FXCollections.observableArrayList();

        String busqueda = txtBuscarCodigoNombre.getText().trim();

        if (!busqueda.isEmpty()) {
            sql.append(" AND (p.codigo ILIKE ? OR p.nombre ILIKE ?)");
            parametros.add("%" + busqueda + "%");
            parametros.add("%" + busqueda + "%");
        }

        if (cmbCategoriaBusqueda.getValue() != null) {
            sql.append(" AND c.nombre = ?");
            parametros.add(cmbCategoriaBusqueda.getValue());
        }

        if (dtpDesde.getValue() != null) {
            sql.append(" AND p.fecha_creacion >= ?");
            parametros.add(java.sql.Date.valueOf(dtpDesde.getValue()));
        }

        if (dtpHasta.getValue() != null) {
            sql.append(" AND p.fecha_creacion <= ?");
            parametros.add(java.sql.Date.valueOf(dtpHasta.getValue()));
        }

        try {
            if (!txtprecioMinimo.getText().isBlank()) {
                double minimo =
                        Double.parseDouble(txtprecioMinimo.getText().trim());

                if (minimo < 0) {
                    throw new NumberFormatException();
                }

                sql.append(" AND p.precio >= ?");
                parametros.add(minimo);
            }

            if (!txtPrecioMaximo.getText().isBlank()) {
                double maximo =
                        Double.parseDouble(txtPrecioMaximo.getText().trim());

                if (maximo < 0) {
                    throw new NumberFormatException();
                }

                sql.append(" AND p.precio <= ?");
                parametros.add(maximo);
            }
        } catch (NumberFormatException ex) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación",
                    "Precio inválido",
                    "Los precios mínimo y máximo deben ser números no negativos.");
            return;
        }

        if (chkExistencia.isSelected()) {
            sql.append(" AND p.stock > 0");
        }

        String orden = cmbOrdenar.getValue();

        if ("Nombre A-Z".equals(orden)) {
            sql.append(" ORDER BY p.nombre ASC");
        } else if ("Nombre Z-A".equals(orden)) {
            sql.append(" ORDER BY p.nombre DESC");
        } else if ("Precio ascendente".equals(orden)) {
            sql.append(" ORDER BY p.precio ASC");
        } else if ("Precio descendente".equals(orden)) {
            sql.append(" ORDER BY p.precio DESC");
        } else if ("Mayor stock".equals(orden)) {
            sql.append(" ORDER BY p.stock DESC");
        } else if ("Menor stock".equals(orden)) {
            sql.append(" ORDER BY p.stock ASC");
        } else if ("Fecha más reciente".equals(orden)) {
            sql.append(" ORDER BY p.fecha_creacion DESC");
        } else if ("Fecha más antigua".equals(orden)) {
            sql.append(" ORDER BY p.fecha_creacion ASC");
        } else {
            sql.append(" ORDER BY p.id_producto ASC");
        }

        productos.clear();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql.toString())
        ) {
            for (int i = 0; i < parametros.size(); i++) {
                statement.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Producto producto = new Producto();

                    producto.setIdProducto(
                            resultSet.getInt("id_producto"));
                    producto.setCodigo(
                            resultSet.getString("codigo"));
                    producto.setNombre(
                            resultSet.getString("nombre"));
                    producto.setDescripcion(
                            resultSet.getString("descripcion"));
                    producto.setPrecio(
                            resultSet.getDouble("precio"));
                    producto.setStock(
                            resultSet.getInt("stock"));
                    producto.setFechaCreacion(
                            resultSet.getDate("fecha_creacion").toLocalDate());
                    producto.setIdCategoria(
                            resultSet.getInt("id_categoria"));
                    producto.setIdMarca(
                            resultSet.getInt("id_marca"));
                    producto.setEstado(
                            resultSet.getInt("estado") == 1);
                    producto.setNombreCategoria(
                            resultSet.getString("nombre_categoria"));
                    producto.setNombreMarca(
                            resultSet.getString("nombre_marca"));

                    productos.add(producto);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "No se pudo realizar la búsqueda", ex.getMessage());
        }
    }
    @FXML
    private void limpiarFiltros() {
        txtBuscarCodigoNombre.clear();
        cmbCategoriaBusqueda.getSelectionModel().clearSelection();
        dtpDesde.setValue(null);
        dtpHasta.setValue(null);
        txtprecioMinimo.clear();
        txtPrecioMaximo.clear();
        chkExistencia.setSelected(false);
        cmbOrdenar.getSelectionModel().clearSelection();

        actualizarTabla();
    }



}
