package de.paulr.homepage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;

public class BatchLatexToHtml {

	private Process process;
	private BufferedReader output;
	private BufferedWriter input;

	public void start() {
		System.out.println("Start Batch-KaTeX process.");
		try {
			ProcessBuilder pb = new ProcessBuilder();
			pb.command("batch-katex");
			pb.redirectInput(Redirect.PIPE);
			process = pb.start();
			output = new BufferedReader(new InputStreamReader(process.getInputStream()));
			input = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String render(String latex) {
		try {
			System.out.println("Rendering LaTeX...");
			input.write(latex);
			input.newLine();
			input.flush();
			System.out.println("Finished writing line.");
			String html = output.readLine();
			System.out.println("Finished rendering LaTeX expression.");
			return html;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void stop() {
		try {
			input.close();
			output.close();
			process.destroy();
			System.out.println("Finished rendering LaTeX expressions.");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
