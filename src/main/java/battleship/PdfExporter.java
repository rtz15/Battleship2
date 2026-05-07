package battleship;

import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.PdfWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class PdfExporter {
	public static final Path DEFAULT_OUTPUT_PATH = Path.of("output", "summary.pdf");

	private PdfExporter() {
	}

	public static Path export(GameSummary summary) throws IOException {
		return export(summary, DEFAULT_OUTPUT_PATH);
	}

	public static Path export(GameSummary summary, Path outputPath) throws IOException {
		Objects.requireNonNull(summary, "summary must not be null");
		Objects.requireNonNull(outputPath, "outputPath must not be null");

		Path absolutePath = outputPath.toAbsolutePath();
		Path parent = absolutePath.getParent();
		if (parent != null) {
			Files.createDirectories(parent);
		}

		Document document = new Document();
		try (OutputStream outputStream = Files.newOutputStream(absolutePath)) {
			PdfWriter.getInstance(document, outputStream);
			document.open();
			writeOverviewSection(document, summary);
			writeFleetStatusSection(document, summary);
			writeMoveDetailsSection(document, summary.moveSummaries());
			writeClosingSection(document, summary);
			document.close();
		} catch (DocumentException e) {
			throw new IOException("Falha ao escrever o PDF", e);
		}

		return absolutePath;
	}

	private static void addLine(Document document, String text) throws DocumentException {
		document.add(new Paragraph(text));
	}

	private static void addBlankLine(Document document) throws DocumentException {
		document.add(new Paragraph(" "));
	}

	private static void writeOverviewSection(Document document, GameSummary summary) throws DocumentException {
		addLine(document, summary.title());
		addBlankLine(document);
		addLine(document, "Resultado final: " + summary.finalResult());
		addLine(document, "Jogadas totais: " + summary.totalMoves());
		addLine(document, "Tiros registados: " + summary.totalShots());
		addLine(document, "Acertos totais: " + summary.totalHits());
		addLine(document, "Tiros repetidos: " + summary.totalRepeatedShots());
		addLine(document, "Tiros exteriores: " + summary.totalOutsideShots());
		addLine(document, "Tiros na agua: " + summary.totalMissedShots());
		addLine(document, "Navios afundados: " + summary.totalSunkShips());
		addLine(document, "Navios restantes: " + summary.remainingShips());
	}

	private static void writeFleetStatusSection(Document document, GameSummary summary) throws DocumentException {
		addBlankLine(document);
		addLine(document, "Estado final da frota:");
		for (String line : summary.fleetStatus()) {
			addLine(document, " - " + line);
		}
	}

	private static void writeMoveDetailsSection(Document document, List<MoveSummary> moveSummaries) throws DocumentException {
		addBlankLine(document);
		addLine(document, "Detalhe das jogadas:");
		for (MoveSummary moveSummary : moveSummaries) {
			writeMoveSection(document, moveSummary);
		}
	}

	private static void writeMoveSection(Document document, MoveSummary moveSummary) throws DocumentException {
		addBlankLine(document);
		addLine(document, "Jogada " + moveSummary.number());
		addLine(document, "  validos=" + moveSummary.validShots()
				+ ", repetidos=" + moveSummary.repeatedShots()
				+ ", exteriores=" + moveSummary.outsideShots()
				+ ", agua=" + moveSummary.missedShots()
				+ ", acertos=" + moveSummary.hits()
				+ ", afundados=" + moveSummary.sunkShips());
		for (ShotSummary shotSummary : moveSummary.shotSummaries()) {
			addLine(document, "  " + shotSummary.position() + " -> " + shotSummary.outcome());
		}
	}

	private static void writeClosingSection(Document document, GameSummary summary) throws DocumentException {
		addBlankLine(document);
		addLine(document, "Mensagem final: " + summary.finalMessage());
	}
}
