package de.ukon.liger.webservice.rest.dtos;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// File tree class to hold metadata
public class FileTree {
    private String name;
    private String path;
    private boolean isDirectory;
    private List<FileTree> children;  // Only for directories

    // Constructor
    public FileTree(String name, String path, boolean isDirectory) {
        this.name = name;
        this.path = path;
        this.isDirectory = isDirectory;
        this.children = new ArrayList<>();
    }

    // Getters and setters (omitted for brevity)

    // Recursive method to build the tree
    public static FileTree buildFileTree(File file) {
        FileTree node = new FileTree(file.getName(), file.getPath(), file.isDirectory());

        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            if (subFiles != null) {
                for (File subFile : subFiles) {
                    node.getChildren().add(buildFileTree(subFile));  // Recursive call
                }
            }
        }

        return node;
    }

    // Usage example
    public FileTree getFileTree(String rootDirPath) {
        File rootDir = new File(rootDirPath);
        return buildFileTree(rootDir);
    }

    // Serialize the file tree to JSON
    public static String convertFileTreeToJson(FileTree fileTree) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(fileTree);
    }


    // Main method for testing
    public static void main(String[] args) {
        // Replace this path with the actual directory you want to test
        String testDirPath = "./grammars";  // Example path

        // Instantiate FileTree and build the tree
        FileTree fileTree = new FileTree("", "", true);
        FileTree root = fileTree.getFileTree(testDirPath);

        // Print the file tree structure
        printFileTree(root, 0);
    }



    // Helper method to print the file tree structure recursively
    private static void printFileTree(FileTree node, int level) {
        // Indentation for tree structure
        String indent = " ".repeat(level * 2);

        // Print node details
        System.out.println(indent + (node.isDirectory() ? "[DIR] " : "[FILE] ") + node.getName());

        // Recursively print children if it's a directory
        for (FileTree child : node.getChildren()) {
            printFileTree(child, level + 1);
        }
    }


    // Getters
    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    // Getter for children
    public List<FileTree> getChildren() {
        return children;
    }

}