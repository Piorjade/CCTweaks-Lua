package org.squiddev.cctweaks.lua.asm;

import com.google.common.collect.Sets;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.squiddev.patcher.transformer.IPatcher;

import java.util.Arrays;
import java.util.HashSet;

import static org.objectweb.asm.Opcodes.*;

/**
 * Adds {@link org.squiddev.cctweaks.api.lua.IMethodDescriptor} to various classes
 */
public class AddMethodDescriptor implements IPatcher {
	private final HashSet<String> names;

	public AddMethodDescriptor() {
		HashSet<String> names = this.names = Sets.newHashSet();
		/*
			Most core APIs don't yield at all so they are trivial to add
			There are some improvements we could make though:
			 - TurtleAPI: probably nothing here
			 - PeripheralAPI: some methods don't yield
			 - CommandAPI: some method's don't yield
			 - Lots of peripherals don't yield
		 */

		names.add("dan200.computercraft.core.apis.BitAPI");
		names.add("dan200.computercraft.core.apis.BufferAPI");
		names.add("dan200.computercraft.core.apis.BufferAPI$BufferLuaObject");
		names.add("dan200.computercraft.core.apis.FSAPI");
		names.add("dan200.computercraft.core.apis.FSAPI$0");
		names.add("dan200.computercraft.core.apis.FSAPI$1");
		names.add("dan200.computercraft.core.apis.FSAPI$2");
		names.add("dan200.computercraft.core.apis.FSAPI$3");
		names.add("dan200.computercraft.core.apis.HTTPAPI");
		names.add("dan200.computercraft.core.apis.OSAPI");
		names.add("dan200.computercraft.core.apis.RedstoneAPI");
		names.add("dan200.computercraft.core.apis.TermAPI");
	}

	@Override
	public boolean matches(String className) {
		return className.startsWith("dan200.computercraft.core.apis.") && names.contains(className);
	}

	@Override
	public ClassVisitor patch(String className, ClassVisitor delegate) throws Exception {
		return new ClassVisitor(ASM5, delegate) {
			@Override
			public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
				super.visit(version, access, name, signature, superName, addInterface(interfaces));
			}

			@Override
			public void visitEnd() {
				// Write willYield method
				MethodVisitor visitor = visitMethod(ACC_PUBLIC | ACC_FINAL, "willYield", "(I)Z", null, null);
				visitor.visitCode();
				visitor.visitInsn(ICONST_0);
				visitor.visitInsn(IRETURN);
				visitor.visitMaxs(1, 2);
				visitor.visitEnd();

				super.visitEnd();
			}
		};
	}

	private static String[] addInterface(String[] interfaces) {
		String[] newInterfaces = Arrays.copyOf(interfaces, interfaces.length + 1);
		newInterfaces[interfaces.length] = "org/squiddev/cctweaks/api/lua/IMethodDescriptor";
		return newInterfaces;
	}
}
