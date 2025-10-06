# GEMINI Project Analysis: MRPGLauncher

## Project Overview

This project is a JavaFX-based launcher application, likely for a Minecraft server or modpack, given the name "MRPGLauncher" and the "onStartButtonClick" method intending to launch Minecraft. The application is built using Apache Maven, which manages its dependencies and the build process.

The main user interface is defined in an FXML file (`main-view.fxml`), separating the UI layout from the application logic. The UI is controlled by a JavaFX controller class (`MainController.js`).

### Key Technologies

*   **Java:** The core programming language.
*   **JavaFX:** Used for creating the graphical user interface.
*   **Apache Maven:** For project build and dependency management.

## Building and Running

### Prerequisites

*   Java Development Kit (JDK) version 22 or higher.
*   Apache Maven.

### Building the Project

To build the project, run the following command in the root directory:

```sh
mvn clean install
```

### Running the Application

To run the application, use the following Maven command:

```sh
mvn clean javafx:run
```

## Development Conventions

*   **UI and Logic Separation:** The project uses FXML (`.fxml` files) to define the user interface layout and Java controller classes to handle UI logic and event handling. This is a standard practice in JavaFX development.
*   **Dependency Management:** All project dependencies are managed in the `pom.xml` file.
*   **Event Handling:** UI event handlers (e.g., for button clicks) are defined in the `MainController` class and linked to the FXML file using the `fx:controller` attribute and `onAction` properties.
