// ECommerceApp.java
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.*;
import javafx.collections.*;
import javafx.beans.property.*;

public class ECommerceApp extends Application {
    private Stage primaryStage;
    // In-memory list of products and cart items
    private ObservableList<Product> products = FXCollections.observableArrayList();
    private ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private String currentUser = null; // stores logged in user

    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("E-Commerce Application");
        
        // Initialize sample products for catalog
        initProducts();
        
        // Start with login scene (Authentication)
        primaryStage.setScene(getLoginScene());
        primaryStage.show();
    }
    
    
    // ======================================================
    // 👥 1. Authentication & Security (Login / Registration)
    // ======================================================
    private Scene getLoginScene() {
        Label userLabel = new Label("Username:");
        TextField userField = new TextField();
        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();
        Button loginButton = new Button("Login");
        Label messageLabel = new Label();
        
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setVgap(10);
        grid.setHgap(10);
        grid.add(userLabel, 0, 0);
        grid.add(userField, 1, 0);
        grid.add(passLabel, 0, 1);
        grid.add(passField, 1, 1);
        grid.add(loginButton, 1, 2);
        grid.add(messageLabel, 1, 3);
        
        // Handle login (simulate checking encrypted credentials)
        loginButton.setOnAction(e -> {
            String username = userField.getText();
            String password = passField.getText();
            if(authenticate(username, password)) {
                // Successful login
                currentUser = username; 
                primaryStage.setScene(getProductCatalogScene());
            } else {
                messageLabel.setText("Invalid credentials. Try 'user' / 'pass'.");
            }
        });
        
        // Simple register button simulation
        Button registerButton = new Button("Register");
        grid.add(registerButton, 1, 4);
        registerButton.setOnAction(e -> {
            // Here you would add registration logic with encryption
            messageLabel.setText("Registration successful. Please login.");
        });
        
        return new Scene(grid, 400, 300);
    }
    
    // ======================================================
    // 👥 2. User Interaction & UI (Product Catalog)
    // ======================================================
    private Scene getProductCatalogScene() {
        Label title = new Label("Product Catalog");
        
        // Table to display products
        TableView<Product> productTable = new TableView<>();
        productTable.setItems(products);
        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());
        TableColumn<Product, Number> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(data -> data.getValue().priceProperty());
        productTable.getColumns().addAll(nameCol, priceCol);
        
        // Filtering: a simple search field
        TextField filterField = new TextField();
        filterField.setPromptText("Search...");
        filterField.textProperty().addListener((obs, oldVal, newVal) -> {
            productTable.setItems(products.filtered(p -> 
                p.getName().toLowerCase().contains(newVal.toLowerCase())
            ));
        });
        
        // View product details
        Button viewDetailsButton = new Button("View Details");
        viewDetailsButton.setOnAction(e -> {
            Product selected = productTable.getSelectionModel().getSelectedItem();
            if(selected != null) {
                primaryStage.setScene(getProductDetailsScene(selected));
            }
        });
        
        // Go to Cart
        Button viewCartButton = new Button("View Cart");
        viewCartButton.setOnAction(e -> {
            primaryStage.setScene(getCartScene());
        });
        
        // Option for Admin (in a real app, restrict access based on role)
        Button adminButton = new Button("Admin Dashboard");
        adminButton.setOnAction(e -> {
            primaryStage.setScene(getAdminDashboardScene());
        });
        
        VBox layout = new VBox(10, title, filterField, productTable, viewDetailsButton, viewCartButton, adminButton);
        layout.setPadding(new Insets(10));
        return new Scene(layout, 600, 400);
    }
    
    // ======================================================
    // 👥 3. Product Details UI
    // ======================================================
    private Scene getProductDetailsScene(Product product) {
        Label nameLabel = new Label("Name: " + product.getName());
        Label descLabel = new Label("Description: " + product.getDescription());
        Label priceLabel = new Label("Price: $" + product.getPrice());
        // For images, JavaFX's ImageView can be used if paths to images are provided.
        
        Button addToCartButton = new Button("Add to Cart");
        addToCartButton.setOnAction(e -> {
            addToCart(product, 1); // default quantity 1
            primaryStage.setScene(getProductCatalogScene());
        });
        
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> primaryStage.setScene(getProductCatalogScene()));
        
        VBox layout = new VBox(10, nameLabel, descLabel, priceLabel, addToCartButton, backButton);
        layout.setPadding(new Insets(10));
        return new Scene(layout, 400, 300);
    }
    
    // ======================================================
    // 🛒 4. Cart & Order Management UI
    // ======================================================
    private Scene getCartScene() {
        Label title = new Label("Your Cart");
        
        TableView<CartItem> cartTable = new TableView<>();
        cartTable.setItems(cartItems);
        TableColumn<CartItem, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> data.getValue().getProduct().nameProperty());
        TableColumn<CartItem, Number> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(data -> data.getValue().quantityProperty());
        TableColumn<CartItem, Number> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(data -> data.getValue().getProduct().priceProperty());
        cartTable.getColumns().addAll(nameCol, qtyCol, priceCol);
        
        Label totalLabel = new Label("Total: $" + calculateTotal());
        Button removeButton = new Button("Remove Selected");
        removeButton.setOnAction(e -> {
            CartItem selected = cartTable.getSelectionModel().getSelectedItem();
            if(selected != null) {
                cartItems.remove(selected);
                totalLabel.setText("Total: $" + calculateTotal());
            }
        });
        
        Button checkoutButton = new Button("Checkout");
        checkoutButton.setOnAction(e -> primaryStage.setScene(getCheckoutScene()));
        
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> primaryStage.setScene(getProductCatalogScene()));
        
        VBox layout = new VBox(10, title, cartTable, totalLabel, removeButton, checkoutButton, backButton);
        layout.setPadding(new Insets(10));
        return new Scene(layout, 600, 400);
    }
    
    // ======================================================
    // 💳 5. Checkout & Payment (Simulated Payment Options)
    // ======================================================
    private Scene getCheckoutScene() {
        Label title = new Label("Checkout");
        Label amountLabel = new Label("Total Amount: $" + calculateTotal());
        
        // Simulated payment options using radio buttons
        ToggleGroup paymentGroup = new ToggleGroup();
        RadioButton cardOption = new RadioButton("Credit/Debit Card");
        cardOption.setToggleGroup(paymentGroup);
        RadioButton paypalOption = new RadioButton("PayPal");
        paypalOption.setToggleGroup(paymentGroup);
        RadioButton walletOption = new RadioButton("Digital Wallet");
        walletOption.setToggleGroup(paymentGroup);
        
        Button payButton = new Button("Pay Now");
        Label messageLabel = new Label();
        payButton.setOnAction(e -> {
            RadioButton selected = (RadioButton) paymentGroup.getSelectedToggle();
            if(selected != null) {
                // Simulated secure payment processing
                messageLabel.setText("Payment Successful using " + selected.getText() + "!");
                // After payment, clear the cart
                cartItems.clear();
            } else {
                messageLabel.setText("Please select a payment method.");
            }
        });
        
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> primaryStage.setScene(getCartScene()));
        
        VBox layout = new VBox(10, title, amountLabel, cardOption, paypalOption, walletOption, payButton, messageLabel, backButton);
        layout.setPadding(new Insets(10));
        return new Scene(layout, 400, 400);
    }
    
    // ======================================================
    // ⚙️ 6. Admin & Inventory Management UI
    // ======================================================
    private Scene getAdminDashboardScene() {
        Label title = new Label("Admin Dashboard - Manage Products");
        TableView<Product> productTable = new TableView<>();
        productTable.setItems(products);
        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());
        TableColumn<Product, Number> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(data -> data.getValue().priceProperty());
        productTable.getColumns().addAll(nameCol, priceCol);
        
        // Form to add a new product
        TextField nameField = new TextField();
        nameField.setPromptText("Product Name");
        TextField descField = new TextField();
        descField.setPromptText("Description");
        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        Button addButton = new Button("Add Product");
        addButton.setOnAction(e -> {
            try {
                double price = Double.parseDouble(priceField.getText());
                Product newProduct = new Product(nameField.getText(), descField.getText(), price);
                products.add(newProduct);
                nameField.clear();
                descField.clear();
                priceField.clear();
            } catch(NumberFormatException ex) {
                System.out.println("Invalid price input.");
            }
        });
        
        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> {
            Product selected = productTable.getSelectionModel().getSelectedItem();
            if(selected != null) {
                products.remove(selected);
            }
        });
        
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> primaryStage.setScene(getProductCatalogScene()));
        
        VBox layout = new VBox(10, title, productTable,
                               new Label("Add New Product:"), nameField, descField, priceField,
                               addButton, deleteButton, backButton);
        layout.setPadding(new Insets(10));
        return new Scene(layout, 600, 400);
    }
    
    // ======================================================
    // Helper Methods and Data Initialization
    // ======================================================
    
    // Simulated authentication (for demo purposes, use fixed credentials)
    private boolean authenticate(String username, String password) {
        return username.equals("user") && password.equals("pass");
    }
    
    // Initialize some sample products for the catalog
    private void initProducts() {
        products.add(new Product("Laptop", "A high performance laptop", 999.99));
        products.add(new Product("Smartphone", "Latest Android smartphone", 499.99));
        products.add(new Product("Headphones", "Noise-cancelling headphones", 199.99));
    }
    
    // Add product to cart; if it already exists, increment quantity
    private void addToCart(Product product, int qty) {
        for (CartItem item : cartItems) {
            if(item.getProduct().equals(product)) {
                item.setQuantity(item.getQuantity() + qty);
                return;
            }
        }
        cartItems.add(new CartItem(product, qty));
    }
    
    // Compute the total cost for all cart items
    private double calculateTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }
        return total;
    }
}

// ======================================================
// Data Model Classes
// ======================================================

// Product class representing product details
class Product {
    private final StringProperty name;
    private final StringProperty description;
    private final DoubleProperty price;
    
    public Product(String name, String description, double price) {
        this.name = new SimpleStringProperty(name);
        this.description = new SimpleStringProperty(description);
        this.price = new SimpleDoubleProperty(price);
    }
    
    public String getName() { return name.get(); }
    public void setName(String value) { name.set(value); }
    public StringProperty nameProperty() { return name; }
    
    public String getDescription() { return description.get(); }
    public void setDescription(String value) { description.set(value); }
    public StringProperty descriptionProperty() { return description; }
    
    public double getPrice() { return price.get(); }
    public void setPrice(double value) { price.set(value); }
    public DoubleProperty priceProperty() { return price; }
    
    // You may override equals() and hashCode() for proper comparisons in cart operations.
}

// CartItem class representing an item in the shopping cart
class CartItem {
    private final Product product;
    private final IntegerProperty quantity;
    
    public CartItem(Product product, int qty) {
        this.product = product;
        this.quantity = new SimpleIntegerProperty(qty);
    }
    
    public Product getProduct() {
        return product;
    }
    
    public int getQuantity() {
        return quantity.get();
    }
    
    public void setQuantity(int qty) {
        quantity.set(qty);
    }
    
    public IntegerProperty quantityProperty() {
        return quantity;
    }
}
