package com.patterns.creational.abstractfactory;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UIComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(UIComponentSystem.class);

    public static void main(String[] args) {
        try {
            // Create Light Theme UI
            UIFactory lightFactory = UIFactoryProvider.getFactory("light");
            createUI("Light Theme", lightFactory);

            System.out.println("\n" + "=".repeat(50) + "\n");

            // Create Dark Theme UI
            UIFactory darkFactory = UIFactoryProvider.getFactory("dark");
            createUI("Dark Theme", darkFactory);

        } catch (Exception e) {
            logger.error("Error in UI component creation", e);
            System.out.println("Failed to create UI components: " + e.getMessage());
        }
    }

    private static void createUI(String theme, UIFactory factory) {
        logger.info("Creating UI with {} theme", theme);
        System.out.println(theme + " UI Components:");
        
        Button button = factory.createButton();
        button.click();
        button.render();

        TextField textField = factory.createTextField();
        textField.input("Hello, World!");
        textField.render();

        Checkbox checkbox = factory.createCheckbox();
        checkbox.toggle();
        checkbox.render();
    }
}

interface UIFactory {
    Button createButton();
    TextField createTextField();
    Checkbox createCheckbox();
}

class LightThemeFactory implements UIFactory {
    private static final Logger logger = LoggerFactory.getLogger(LightThemeFactory.class);

    @Override
    public Button createButton() {
        logger.debug("Creating light theme button");
        return new LightButton();
    }

    @Override
    public TextField createTextField() {
        logger.debug("Creating light theme text field");
        return new LightTextField();
    }

    @Override
    public Checkbox createCheckbox() {
        logger.debug("Creating light theme checkbox");
        return new LightCheckbox();
    }
}

class DarkThemeFactory implements UIFactory {
    private static final Logger logger = LoggerFactory.getLogger(DarkThemeFactory.class);

    @Override
    public Button createButton() {
        logger.debug("Creating dark theme button");
        return new DarkButton();
    }

    @Override
    public TextField createTextField() {
        logger.debug("Creating dark theme text field");
        return new DarkTextField();
    }

    @Override
    public Checkbox createCheckbox() {
        logger.debug("Creating dark theme checkbox");
        return new DarkCheckbox();
    }
}

class UIFactoryProvider {
    private static final Logger logger = LoggerFactory.getLogger(UIFactoryProvider.class);
    private static final Map<String, UIFactory> factories = new HashMap<>();

    static {
        factories.put("light", new LightThemeFactory());
        factories.put("dark", new DarkThemeFactory());
    }

    public static UIFactory getFactory(String theme) {
        UIFactory factory = factories.get(theme.toLowerCase());
        if (factory == null) {
            logger.error("Unsupported theme requested: {}", theme);
            throw new IllegalArgumentException("Unsupported theme: " + theme);
        }
        return factory;
    }
}

// UI Component interfaces
interface Button {
    void click();
    void render();
}

interface TextField {
    void input(String text);
    void render();
}

interface Checkbox {
    void toggle();
    void render();
}

// Light Theme Components
class LightButton implements Button {
    private static final Logger logger = LoggerFactory.getLogger(LightButton.class);

    @Override
    public void click() {
        logger.info("Light button clicked");
        System.out.println("⚪ Light Button clicked");
    }

    @Override
    public void render() {
        System.out.println("Rendering Light Button: [ Click Me ]");
    }
}

class LightTextField implements TextField {
    private String text = "";
    private static final Logger logger = LoggerFactory.getLogger(LightTextField.class);

    @Override
    public void input(String text) {
        this.text = text;
        logger.info("Text input to light text field: {}", text);
    }

    @Override
    public void render() {
        System.out.println("Rendering Light TextField: |" + text + "| (Light bg)");
    }
}

class LightCheckbox implements Checkbox {
    private boolean checked = false;
    private static final Logger logger = LoggerFactory.getLogger(LightCheckbox.class);

    @Override
    public void toggle() {
        checked = !checked;
        logger.info("Light checkbox toggled: {}", checked);
    }

    @Override
    public void render() {
        System.out.println("Rendering Light Checkbox: [" + (checked ? "✓" : " ") + "] (Light bg)");
    }
}

// Dark Theme Components
class DarkButton implements Button {
    private static final Logger logger = LoggerFactory.getLogger(DarkButton.class);

    @Override
    public void click() {
        logger.info("Dark button clicked");
        System.out.println("⚫ Dark Button clicked");
    }

    @Override
    public void render() {
        System.out.println("Rendering Dark Button: [ Click Me ]");
    }
}

class DarkTextField implements TextField {
    private String text = "";
    private static final Logger logger = LoggerFactory.getLogger(DarkTextField.class);

    @Override
    public void input(String text) {
        this.text = text;
        logger.info("Text input to dark text field: {}", text);
    }

    @Override
    public void render() {
        System.out.println("Rendering Dark TextField: |" + text + "| (Dark bg)");
    }
}

class DarkCheckbox implements Checkbox {
    private boolean checked = false;
    private static final Logger logger = LoggerFactory.getLogger(DarkCheckbox.class);

    @Override
    public void toggle() {
        checked = !checked;
        logger.info("Dark checkbox toggled: {}", checked);
    }

    @Override
    public void render() {
        System.out.println("Rendering Dark Checkbox: [" + (checked ? "✓" : " ") + "] (Dark bg)");
    }
}
