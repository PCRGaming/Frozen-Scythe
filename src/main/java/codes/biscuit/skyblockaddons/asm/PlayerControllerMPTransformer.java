package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class PlayerControllerMPTransformer implements ITransformer {


    /**
     * {@link net.minecraft.client.multiplayer.PlayerControllerMP}
     */
    @Override
    public String[] getClassName() {
        return new String[]{"net.minecraft.client.multiplayer.PlayerControllerMP"};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) { // Loop through all methods inside of the class.

            String methodName = mapMethodName(classNode, methodNode); // Map all of the method names.
            if (nameMatches(methodName,"clickBlock", "func_180511_b")) {

                // Objective:
                // Find: Method head.
                // Insert:   ReturnValue returnValue = new ReturnValue();
                //           PlayerControllerMPHook.onPlayerDamageBlock(loc, returnValue);
                //           if (returnValue.isCancelled()) {
                //               return false;
                //           }

                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insertOnClickBlock());

            } else if (nameMatches(methodName,"onPlayerDestroyBlock", "func_178888_a")) {

                // Objective:
                // Find: Method head.
                // Insert: PlayerControllerMPHook.onPlayerDestroyBlock();

                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "codes/biscuit/skyblockaddons/asm/hooks/PlayerControllerMPHook", "onPlayerDestroyBlock", "()V", false));
            } else if (nameMatches(methodName,"windowClick", "func_78753_a")) {

            // Objective:
            // Find: Method head.
                // Insert:   ReturnValue returnValue = new ReturnValue();
                //           PlayerControllerMPHook.onWindowClick(slotId, mode, playerIn, returnValue);
                //           if (returnValue.isCancelled()) {
                //               return null;
                //           }

            methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insertOnWindowClick());
        }
        }
    }

    private InsnList insertOnClickBlock() {
        InsnList list = new InsnList();

        list.add(new TypeInsnNode(Opcodes.NEW, "codes/biscuit/skyblockaddons/asm/hooks/ReturnValue"));
        list.add(new InsnNode(Opcodes.DUP)); // ReturnValue returnValue = new ReturnValue();
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "codes/biscuit/skyblockaddons/asm/hooks/ReturnValue", "<init>", "()V", false));
        list.add(new VarInsnNode(Opcodes.ASTORE, 5));

        list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // loc
        list.add(new VarInsnNode(Opcodes.ALOAD, 5)); // PlayerControllerMPHook.onPlayerDamageBlock(loc, returnValue);
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/PlayerControllerMPHook", "onPlayerDamageBlock",
                "(Lnet/minecraft/util/BlockPos;Lcodes/biscuit/skyblockaddons/asm/hooks/ReturnValue;)V", false));

        list.add(new VarInsnNode(Opcodes.ALOAD, 5));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "codes/biscuit/skyblockaddons/asm/hooks/ReturnValue", "isCancelled",
                "()Z", false));
        LabelNode notCancelled = new LabelNode(); // if (returnValue.isCancelled())
        list.add(new JumpInsnNode(Opcodes.IFEQ, notCancelled));

        list.add(new InsnNode(Opcodes.ICONST_0)); // return false;
        list.add(new InsnNode(Opcodes.IRETURN));
        list.add(notCancelled);

        return list;
    }

    private InsnList insertOnWindowClick() {
        InsnList list = new InsnList();

        list.add(new TypeInsnNode(Opcodes.NEW, "codes/biscuit/skyblockaddons/asm/hooks/ReturnValue"));
        list.add(new InsnNode(Opcodes.DUP)); // ReturnValue returnValue = new ReturnValue();
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "codes/biscuit/skyblockaddons/asm/hooks/ReturnValue", "<init>", "()V", false));
        list.add(new VarInsnNode(Opcodes.ASTORE, 8));

        list.add(new VarInsnNode(Opcodes.ILOAD, 2)); // slotId
        list.add(new VarInsnNode(Opcodes.ILOAD, 4)); // mode
        list.add(new VarInsnNode(Opcodes.ALOAD, 5)); // playerIn
        list.add(new VarInsnNode(Opcodes.ALOAD, 8)); // PlayerControllerMPHook.onWindowClick(slotId, mode, playerIn, returnValue);
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/PlayerControllerMPHook", "onWindowClick",
               "(IILnet/minecraft/entity/player/EntityPlayer;Lcodes/biscuit/skyblockaddons/asm/hooks/ReturnValue;)V", false));

        list.add(new VarInsnNode(Opcodes.ALOAD, 8));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "codes/biscuit/skyblockaddons/asm/hooks/ReturnValue", "isCancelled",
                "()Z", false));
        LabelNode notCancelled = new LabelNode(); // if (returnValue.isCancelled())
        list.add(new JumpInsnNode(Opcodes.IFEQ, notCancelled));

        list.add(new InsnNode(Opcodes.ACONST_NULL)); // return null;
        list.add(new InsnNode(Opcodes.ARETURN));
        list.add(notCancelled);

        return list;
    }
}