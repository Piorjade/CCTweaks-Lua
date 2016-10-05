package org.squiddev.cctweaks.lua.asm;

import dan200.computercraft.core.computer.Computer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.squiddev.patcher.transformer.IPatcher;
import org.squiddev.patcher.visitors.FindingVisitor;

import static org.objectweb.asm.Opcodes.GETSTATIC;

/**
 * Allows changing the bios file
 *
 * @see org.squiddev.cctweaks.lua.Config.Computer#biosPath
 * @see Computer#initLua()
 */
public class CustomBios implements IPatcher {
	@Override
	public boolean matches(String className) {
		return className.equals("dan200.computercraft.core.computer.Computer");
	}

	@Override
	public ClassVisitor patch(String className, ClassVisitor delegate) throws Exception {
		return new FindingVisitor(
			delegate,
			new LdcInsnNode("/assets/computercraft/lua/bios.lua")
		) {
			@Override
			public void handle(InsnList nodes, MethodVisitor visitor) {
				visitor.visitFieldInsn(GETSTATIC, "org/squiddev/cctweaks/lua/Config$Computer", "biosPath", "Ljava/lang/String;");
			}
		}.onMethod("initLua").mustFind();
	}
}
