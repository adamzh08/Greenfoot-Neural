How to run the project (bash): './mvnw clean javafx:run'

Explanation:
'mvnw': maven wrapper,
'clean': cleans the build directory,
'javafx:run': compiles the project and launches the JavaFX application

Greenfoot-Neural/
├── .gitignore
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties
├── mvnw
├── mvnw.cmd
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/
        │   ├── GameUtils/
        │   │   ├── Ant.java
        │   │   ├── GameManager.java
        │   │   └── MazeGenerator.java
        │   ├── Main.java
        │   ├── NN/
        │   │   ├── ActivationFunction_I.java
        │   │   ├── ActivationFunctions.java
        │   │   ├── Layer.java
        │   │   └── Network.java
        │   └── Physics/
        │       ├── CircleWall.java
        │       ├── Intersection.java
        │       ├── LineSegmentWall.java
        │       ├── RayCast.java
        │       ├── Utils.java
        │       └── Vector2D.java
        └── resources/
            └── ant.png
