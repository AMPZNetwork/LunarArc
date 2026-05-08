package io.ampznetwork.lunararc.scratch;

import io.papermc.paper.registry.RegistryAccess;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class RegistryInspector {
    public static void inspect() {
        try {
            Class<?> clazz = RegistryAccess.class;
            System.out.println("Inspecting " + clazz.getName());
            for (Field field : clazz.getDeclaredFields()) {
                System.out.println("Field: " + field.getName() + " Type: " + field.getType().getName() + " Modifiers: " + Modifier.toString(field.getModifiers()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
