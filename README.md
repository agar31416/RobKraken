##
## Copyright © 2025 Angel Gael Aguilar Reyes. Todos los derechos reservados.
## Este material es para distribución exclusiva entre amigos del grupo.
## Prohibida su reproducción, modificación o distribución sin autorización expresa.
##

## Getting Started

Welcome to the VS Code Java world. Here is a guideline to help you get started to write Java code in Visual Studio Code.

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.

## Dependency Management

The `JAVA PROJECTS` view allows you to manage your dependencies. More details can be found [here](https://github.com/microsoft/vscode-java-dependency#manage-dependencies).

/*
 * CÓMO AGREGAR UNA LIBRERÍA JAR EN VISUAL STUDIO CODE
 * ==================================================
 * 
 * NOTA: Este documento es de uso exclusivo para miembros autorizados del grupo.
 * Distribución prohibida sin permiso del autor: Angel Gael Aguilar Reyes.
 * 
 * Si tienes problemas con imports que no se resuelven (ej: import com.fazecast.jSerialComm.*;)
 * 
 * MÉTODO MANUAL PARA AGREGAR JAR:
 * 1. Presionar: Ctrl + Shift + P
 * 2. Escribir: "Java: Configure Classpath"
 * 3. Click en el botón "+" junto a "Referenced Libraries" 
 * 4. Navegar y seleccionar tu archivo .jar
 * 
 * MÉTODO ALTERNATIVO CON CONFIGURACIÓN:
 * - Crear/editar .vscode/settings.json con:
 * {
 *     "java.project.sourcePaths": ["src"],
 *     "java.project.outputPath": "bin",
 *     "java.project.referencedLibraries": [
 *         "lib/**/*.jar"
 *     ]
 * }
 * - Colocar el archivo .jar en la carpeta lib/
 * - Ejecutar: Ctrl + Shift + P → "Java: Rebuild Projects"
 * 
 * VERIFICAR QUE FUNCIONÓ:
 * - En el panel izquierdo, expandir "JAVA PROJECTS"
 * - Verificar que aparezca en "Referenced Libraries"
 */