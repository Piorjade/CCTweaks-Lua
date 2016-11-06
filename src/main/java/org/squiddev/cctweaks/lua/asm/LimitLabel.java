package org.squiddev.cctweaks.lua.asm;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.squiddev.patcher.transformer.IPatcher;
import org.squiddev.patcher.visitors.FindingVisitor;

import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

/**
 * Adds a 32 character limit on disk drive labels. This is the same limit as used in the OS API.
 *
 * Also optionally removes the ability to use non ASCII printable characters.
 *
 * @see dan200.computercraft.shared.peripheral.diskdrive.DiskDrivePeripheral#callMethod(IComputerAccess, ILuaContext, int, Object[])
 * @see dan200.computercraft.core.apis.OSAPI#callMethod(ILuaContext, int, Object[])
 */
public class LimitLabel implements IPatcher {
	@Override
	public boolean matches(String className) {
		return className.equals("dan200.computercraft.shared.peripheral.diskdrive.DiskDrivePeripheral")
			|| className.equals("dan200.computercraft.core.apis.OSAPI");
	}

	@Override
	public ClassVisitor patch(String className, ClassVisitor delegate) throws Exception {
		if (className.equals("dan200.computercraft.core.apis.OSAPI")) {
			return new LabelVisitor(
				delegate,
				new MethodInsnNode(INVOKEINTERFACE, "dan200/computercraft/core/apis/IAPIEnvironment", "setLabel", "(Ljava/lang/String;)V", true)
			);
		} else {
			return new LabelVisitor(delegate,
				new MethodInsnNode(INVOKEINTERFACE, "dan200/computercraft/api/media/IMedia", "setLabel", "(Lnet/minecraft/item/ItemStack;Ljava/lang/String;)Z", true)
			);
		}
	}

	private static final class LabelVisitor extends FindingVisitor {
		public LabelVisitor(ClassVisitor classVisitor, AbstractInsnNode... nodes) {
			super(classVisitor, nodes);

			onMethod("callMethod");
			once();
			mustFind();
		}

		@Override
		public void handle(InsnList nodes, MethodVisitor visitor) {
			visitor.visitMethodInsn(INVOKESTATIC, "org/squiddev/cctweaks/lua/lib/LuaHelpers", "limitLabel", "(Ljava/lang/String;)Ljava/lang/String;", false);
			nodes.accept(visitor);
		}
	}
}
