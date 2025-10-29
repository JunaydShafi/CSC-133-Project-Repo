package pkgSlRenderEngine;

// Top-level public class
public class SlRenderer {

    // --- Nested Interface ---
    public interface GeneratePlainVA {
        void generate();
    }

    // --- Nested Static Class ---
    public static class InnerRenderer {
        public void render() {
            System.out.println("Nested class rendering...");
        }
    }

    // Optional: constructor to show IDE that this class is "active"
    public SlRenderer() {
        System.out.println("SlRenderer instance created");
    }

    // Optional: a method to use the nested interface
    public void useInterface(GeneratePlainVA generator) {
        generator.generate();
    }

    // Optional: a method to demonstrate nested class usage
    public void demoInnerRenderer() {
        InnerRenderer ir = new InnerRenderer();
        ir.render();
    }

    // Main method to test
    public static void main(String[] args) {
        SlRenderer sr = new SlRenderer();

        // Using the nested interface
        sr.useInterface(() -> System.out.println("Nested interface in action!"));

        // Using the nested class
        sr.demoInnerRenderer();
    }
}
