package battleship;

import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfExporterTest {

	@Test
	void exportsDeterministicSummaryPdf() throws Exception {
		Fleet fleet = new Fleet();
		fleet.addShip(new Barge(Compass.NORTH, new Position(0, 0)));

		Game game = new Game(fleet);
		game.fireShots(List.of(
				new Position(0, 0),
				new Position(0, 1),
				new Position(0, 2)
		));

		Path pdfPath = PdfExporter.DEFAULT_OUTPUT_PATH;
		Files.deleteIfExists(pdfPath);

		Path generatedPdf = game.exportSummary(pdfPath);
		assertTrue(Files.exists(generatedPdf), "O PDF deveria ser gerado.");

		String text = extractText(generatedPdf);
		assertTrue(text.contains("Resumo da simulacao Battleship"));
		assertTrue(text.contains("Jogadas totais: 1"));
		assertTrue(text.contains("Navios restantes: 0"));
		assertTrue(text.contains("A1 -> Afundou Barca"));
		assertTrue(text.contains("A2 -> Agua"));
		assertTrue(text.contains("A3 -> Agua"));
	}

	private String extractText(Path pdfPath) throws IOException {
		try (PdfReader reader = new PdfReader(Files.readAllBytes(pdfPath))) {
			PdfTextExtractor extractor = new PdfTextExtractor(reader);
			StringBuilder text = new StringBuilder();
			for (int page = 1; page <= reader.getNumberOfPages(); page++) {
				text.append(extractor.getTextFromPage(page));
			}
			return text.toString();
		}
	}
}
