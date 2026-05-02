package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for class PdfExporter.
 * Author: Eduardo Sousa
 * Date: 2026-04-26 17:24
 * Cyclomatic Complexity:
 * - export(GameSummary): 1
 * - export(GameSummary, Path): 6
 */
class PdfExporterTest {

	@AfterEach
	void cleanDefaultPdf() throws IOException {
		Files.deleteIfExists(PdfExporter.DEFAULT_OUTPUT_PATH);
	}

	@Test
	@DisplayName("PdfExporter writes the deterministic simulator summary to PDF")
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

	@Test
	@DisplayName("PdfExporter rejects a null summary")
	void exportRejectsNullSummary() {
		NullPointerException exception = assertThrows(
				NullPointerException.class,
				() -> PdfExporter.export(null, Path.of("output", "ignored.pdf")),
				"Error: expected PdfExporter to reject a null summary."
		);

		assertEquals("summary must not be null", exception.getMessage(), "Error: expected the null-summary guard message to match the implementation.");
	}

	@Test
	@DisplayName("PdfExporter rejects a null output path")
	void exportRejectsNullOutputPath() {
		NullPointerException exception = assertThrows(
				NullPointerException.class,
				() -> PdfExporter.export(sampleSummary(), null),
				"Error: expected PdfExporter to reject a null output path."
		);

		assertEquals("outputPath must not be null", exception.getMessage(), "Error: expected the null-outputPath guard message to match the implementation.");
	}

	@Test
	@DisplayName("PdfExporter creates parent directories and serializes summary details")
	void exportCreatesParentDirectoriesAndSerializesSummaryDetails(@TempDir Path tempDir) throws Exception {
		Path nestedPdf = tempDir.resolve("reports").resolve("pdf").resolve("summary.pdf");

		Path exportedPdf = PdfExporter.export(sampleSummary(), nestedPdf);
		String text = extractText(exportedPdf);

		assertAll(
				() -> assertEquals(nestedPdf.toAbsolutePath(), exportedPdf, "Error: expected export to return the absolute output path."),
				() -> assertTrue(Files.exists(exportedPdf), "Error: expected the nested PDF file to be created."),
				() -> assertTrue(text.contains("Resumo da simulacao Battleship"), "Error: expected the PDF to contain the summary title."),
				() -> assertTrue(text.contains("Resultado final: Todos os navios do jogador foram afundados"), "Error: expected the PDF to contain the final result line."),
				() -> assertTrue(text.contains("Estado final da frota:"), "Error: expected the PDF to contain the fleet status section."),
				() -> assertTrue(text.contains(" - Barca @ A1 - Afundado"), "Error: expected the PDF to contain the fleet status entry."),
				() -> assertTrue(text.contains("Jogada 1"), "Error: expected the PDF to contain the move summary header."),
				() -> assertTrue(text.contains("A1 -> Afundou Barca"), "Error: expected the PDF to contain the first shot summary."),
				() -> assertTrue(text.contains("B2 -> Agua"), "Error: expected the PDF to contain the second shot summary."),
				() -> assertTrue(text.contains("Mensagem final: Maldito sejas, Java Sparrow"), "Error: expected the PDF to contain the final message.")
		);
	}

	@Test
	@DisplayName("PdfExporter default overload writes to the standard output path")
	void exportWithoutExplicitPathUsesDefaultOutputLocation() throws Exception {
		Path exportedPdf = PdfExporter.export(sampleSummary());

		assertAll(
				() -> assertEquals(PdfExporter.DEFAULT_OUTPUT_PATH.toAbsolutePath(), exportedPdf, "Error: expected the default overload to return the default absolute path."),
				() -> assertTrue(Files.exists(exportedPdf), "Error: expected the default overload to create the PDF file."),
				() -> assertTrue(extractText(exportedPdf).contains("Jogadas totais: 2"), "Error: expected the generated PDF to contain the total move count from the sample summary.")
		);
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

	private GameSummary sampleSummary() {
		List<String> fleetStatus = new ArrayList<>();
		fleetStatus.add("Barca @ A1 - Afundado");
		fleetStatus.add("Fragata @ C3 - A flutuar");

		List<ShotSummary> firstMoveShots = List.of(
				new ShotSummary("A1", "Afundou Barca"),
				new ShotSummary("B2", "Agua")
		);

		List<ShotSummary> secondMoveShots = List.of(
				new ShotSummary("C3", "Acertou Fragata")
		);

		List<MoveSummary> moveSummaries = List.of(
				new MoveSummary(1, 2, 0, 1, 1, 1, 1, firstMoveShots),
				new MoveSummary(2, 1, 1, 1, 0, 1, 0, secondMoveShots)
		);

		return new GameSummary(
				"Resumo da simulacao Battleship",
				"Todos os navios do jogador foram afundados",
				"Maldito sejas, Java Sparrow",
				2,
				6,
				2,
				1,
				2,
				1,
				1,
				0,
				fleetStatus,
				moveSummaries
		);
	}
}
