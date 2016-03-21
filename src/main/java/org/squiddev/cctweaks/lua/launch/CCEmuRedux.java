package org.squiddev.cctweaks.lua.launch;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.squiddev.luaj.luajc.utils.AsmUtils;
import org.squiddev.patcher.transformer.IPatcher;
import org.squiddev.patcher.visitors.FindingVisitor;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.squiddev.cctweaks.lua.launch.Launcher.*;

/**
 * Special launcher for CCEmuRedux
 */
public class CCEmuRedux {
	public static void main(String[] args) throws Exception {
		Integer width = parseNumber("cctweaks.ccemu.width");
		Integer height = parseNumber("cctweaks.ccemu.height");

		RewritingLoader loader = setupLoader();

		if (width != null || height != null) {
			loader.chain.add(new CCEmuPatcher(
				width == null ? 51 : width,
				height == null ? 19 : height
			));
		}

		loader.chain.finalise();
		execute(loader, "com.xtansia.ccemu.desktop.DesktopLauncher", new String[0]);
	}


	public static class CCEmuPatcher implements IPatcher {
		private final int width;
		private final int height;

		public CCEmuPatcher(int width, int height) {
			this.width = width;
			this.height = height;
		}

		@Override
		public boolean matches(String className) {
			return className.equals("com.xtansia.ccemu.computercraft.computer.ComputerCraftComputer");
		}

		@Override
		public ClassVisitor patch(String className, ClassVisitor delegate) throws Exception {
			return new FindingVisitor(delegate,
				new VarInsnNode(ALOAD, 3),
				new MethodInsnNode(INVOKEVIRTUAL, "com/xtansia/ccemu/api/computer/ComputerType", "getTerminalWidth", "()I", false),
				new VarInsnNode(ALOAD, 3),
				new MethodInsnNode(INVOKEVIRTUAL, "com/xtansia/ccemu/api/computer/ComputerType", "getTerminalHeight", "()I", false)
			) {
				@Override
				public void handle(InsnList nodes, MethodVisitor visitor) {
					AsmUtils.constantOpcode(visitor, width);
					AsmUtils.constantOpcode(visitor, height);
				}
			}.onMethod("<init>").once().mustFind();
		}
	}

}
