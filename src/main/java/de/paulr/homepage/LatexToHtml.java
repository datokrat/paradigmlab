package de.paulr.homepage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LatexToHtml {

	public static int ctr = 0;

	public static String render(String latex) {
		try {
			System.out.println("Rendering LaTeX (" + ++ctr + ")...");
			ProcessBuilder pb = new ProcessBuilder();
			pb.command("katex", latex);
			Process process = pb.start();
			if (process.waitFor() != 0) {
				throw new RuntimeException("katex process exited with non-zero exit code");
			}

			String result = new String(process.getInputStream().readAllBytes(),
				StandardCharsets.UTF_8);
			System.out.println("Rendered LaTeX.");
			return result;
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}
