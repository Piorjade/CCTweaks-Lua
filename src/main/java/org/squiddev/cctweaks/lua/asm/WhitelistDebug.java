package org.squiddev.cctweaks.lua.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.*;
import org.squiddev.patcher.transformer.IPatcher;
import org.squiddev.patcher.visitors.FindingVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * Whitelist specific globals
 */
public class WhitelistDebug implements IPatcher {
	@Override
	public boolean matches(String className) {
		return className.equals("dan200.computercraft.core.lua.LuaJLuaMachine");
	}

	@Override
	public ClassVisitor patch(String className, ClassVisitor delegate) throws Exception {
		return new FindingVisitor(delegate,
			new VarInsnNode(ALOAD, 0),
			new FieldInsnNode(GETFIELD, "dan200/computercraft/core/lua/LuaJLuaMachine", "m_globals", "Lorg/luaj/vm2/LuaValue;"),
			new LdcInsnNode("debug"),
			new FieldInsnNode(GETSTATIC, "org/luaj/vm2/LuaValue", "NIL", "Lorg/luaj/vm2/LuaValue;"),
			new MethodInsnNode(INVOKEVIRTUAL, "org/luaj/vm2/LuaValue", "set", "(Ljava/lang/String;Lorg/luaj/vm2/LuaValue;)V", false)
		) {
			@Override
			public void handle(InsnList nodes, MethodVisitor visitor) {
				Label blacklistLabel = new Label();

				visitor.visitFieldInsn(GETSTATIC, "org/squiddev/cctweaks/lua/Config$APIs", "debug", "Z");
				visitor.visitJumpInsn(IFNE, blacklistLabel);

				nodes.accept(visitor);

				visitor.visitLabel(blacklistLabel);
			}
		}.onMethod("<init>").once().mustFind();
	}
}
