package tests;

import java.util.List;

import models.Question;
import models.User;
import models.database.Database;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;

public class SearchTest extends UnitTest {

	private Question fulltextPositive;
	private Question fulltextNegative;
	private Question taggedNegative;
	private Question taggedPositive;

	@Before
	public void setUp() throws Exception {
		Database.clear();
		User jack = new User("Jack","", "jack@jack.com");
		User jill = new User("Jill","", "jill@jill.com");
		fulltextPositive = new Question(jack,"This is relevant.");
		fulltextNegative = new Question(jill,"This is not.");
		taggedPositive   = new Question(jack,"This is about an important thing.");
		taggedNegative   = new Question(jack,"This is not about anything important.");
		taggedPositive.setTagString("relevant");
		taggedNegative.setTagString("plop");
		fulltextPositive.answer(jill, "My answer");
	}

	@Test
	public void shouldFindFulltext() {
		assertTrue(Database.get().questions().searchFor("relevant").contains(
				fulltextPositive));
	}

	@Test
	public void shouldntFindFulltextNegative() {
		assertFalse(Database.get().questions().searchFor("relevant").contains(
				fulltextNegative));
	}

	@Test
	public void shouldFindByTag() {
		assertTrue(Database.get().questions().searchFor("relevant")
				.contains(taggedPositive));
	}

	@Test
	public void shouldFindByTagOnly() {
		assertFalse(Database.get().questions().searchFor("tag:relevant")
				.contains(fulltextPositive));
		assertTrue(Database.get().questions().searchFor("tag:relevant")
				.contains(taggedPositive));
	}

	@Test
	public void shouldntFindByTagNegative() {
		assertFalse(Database.get().questions().searchFor("relevant")
				.contains(taggedNegative));
	}

	@Test
	public void shouldntSearchForStupidWords() {
		assertTrue(Database.get().questions().searchFor("is").isEmpty());
		assertTrue(Database.get().questions().searchFor("???").isEmpty());
	}
	
	@Test
	public void shouldSearchMixedWord() {
		assertTrue(Database.get().questions().searchFor("is relevant").contains(fulltextPositive));
		assertTrue(Database.get().questions().searchFor("is relevant").contains(taggedPositive));
		assertTrue(Database.get().questions().searchFor("??? relevant")
				.contains(taggedPositive));
	}

	@Test
	public void shouldBeANDSearch() {
		assertEquals(Database.get().questions().searchFor("relevant").size(), 2);
		List<Question> relevantImportant = Database.get().questions()
				.searchFor("relevant important");
		assertEquals(relevantImportant.size(), 1);
		assertTrue(relevantImportant.contains(taggedPositive));
		assertTrue(Database.get().questions().searchFor("relevant dummy")
				.isEmpty());
	}

	@Test
	public void shouldFindUsername() {
		List<Question> jills = Database.get().questions().searchFor("jill");
		assertEquals(jills.size(), 2);
		assertTrue(jills.contains(fulltextPositive));
		assertTrue(jills.contains(fulltextNegative));

		List<Question> jackImportant = Database.get().questions()
				.searchFor("jack important");
		assertEquals(jackImportant.size(), 2);
		assertTrue(jackImportant.contains(taggedPositive));
		assertTrue(jackImportant.contains(taggedNegative));

		List<Question> jackTagged = Database.get().questions()
				.searchFor("jack tag:plop");
		assertEquals(jackTagged.size(), 1);
		assertTrue(jackTagged.contains(taggedNegative));
	}
}
