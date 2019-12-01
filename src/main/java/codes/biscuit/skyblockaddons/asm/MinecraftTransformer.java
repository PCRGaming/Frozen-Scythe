package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.tweaker.SkyblockAddonsTransformer;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class MinecraftTransformer implements ITransformer {

    /**
     * {@link net.minecraft.client.Minecraft}
     */
    @Override
    public String[] getClassName() {
        return new String[]{"net.minecraft.client.Minecraft"};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) { // Loop through all methods inside of the class.

            String methodName = mapMethodName(classNode, methodNode); // Map all of the method names.
            if (nameMatches(methodName, "refreshResources", "func_110436_a")) {

                // Objective:
                // Find: Method return.
                // Insert: MinecraftHook.refreshResources(this.mcResourceManager);

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof InsnNode && abstractNode.getOpcode() == Opcodes.RETURN) {
                        methodNode.instructions.insertBefore(abstractNode, insertOnRefreshResources());
                        break;
                    }
                }
            }
            if (nameMatches(methodName, "rightClickMouse", "func_147121_ag")) {

                // Objective:
                // Find: Before "this.rightClickDelayTimer = 4;"
                // Insert:   ReturnValue returnValue = new ReturnValue();
                //           MinecraftHook.rightClickMouse(returnValue);
                //           if (returnValue.isCancelled()) {
                //               return;
                //           }

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof InsnNode && abstractNode.getOpcode() == Opcodes.ICONST_4) {
                        methodNode.instructions.insertBefore(abstractNode.getPrevious(), insertRightClickMouse());
                        break;
                    }
                }
            }
        }
    }

    private InsnList insertOnRefreshResources() {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.

        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/Minecraft", // mcResourceManager
               SkyblockAddonsTransformer.DEOBFUSCATED ? "mcResourceManager" : "field_110451_am", "Lnet/minecraft/client/resources/IReloadableResourceManager;"));

        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/MinecraftHook", "onRefreshResources",
                "(Lnet/minecraft/client/resources/IReloadableResourceManager;)V", false)); // MinecraftHook.refreshResources(this.mcResourceManager);

        return list;
    }

    private InsnList insertRightClickMouse() {
        InsnList list = new InsnList();

        list.add(new TypeInsnNode(Opcodes.NEW, "codes/biscuit/skyblockaddons/asm/hooks/ReturnValue"));
        list.add(new InsnNode(Opcodes.DUP)); // ReturnValue returnValue = new ReturnValue();
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "codes/biscuit/skyblockaddons/asm/hooks/ReturnValue", "<init>", "()V", false));
        list.add(new VarInsnNode(Opcodes.ASTORE, 6));

        list.add(new VarInsnNode(Opcodes.ALOAD, 6)); // MinecraftHook.rightClickMouse(returnValue);
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/MinecraftHook", "rightClickMouse",
                "(Lcodes/biscuit/skyblockaddons/asm/hooks/ReturnValue;)V", false));

        list.add(new VarInsnNode(Opcodes.ALOAD, 6));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "codes/biscuit/skyblockaddons/asm/hooks/ReturnValue", "isCancelled",
                "()Z", false));
        LabelNode notCancelled = new LabelNode(); // if (returnValue.isCancelled())
        list.add(new JumpInsnNode(Opcodes.IFEQ, notCancelled));

        list.add(new InsnNode(Opcodes.RETURN)); // return;
        list.add(notCancelled);

        return list;
    }
}