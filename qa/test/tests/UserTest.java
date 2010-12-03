package tests;

import java.text.ParseException;

import models.Question;
import models.User;
import models.database.Database;
import models.helpers.Tools;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;

public class UserTest extends UnitTest {

	@Before
	public void setUp() {
		Database.clear();
	}

	@Test
	public void shouldCreateUser() {
		User user = new User("Jack", "jack", "jack@jack.com");
		assertTrue(user != null);
	}

	@Test
	public void shouldBeCalledJack() {
		User user = new User("Jack", "jack", "jack@jack.com");
		assertEquals(user.getName(), "Jack");
	}

	@Test
	public void checkUsernameAvailable() {
		assertTrue(Database.get().users().isAvailable("JaneSmith"));
		Database.get().users().register("JaneSmith", "janesmith", "jane@smith.com");
		assertFalse(Database.get().users().isAvailable("JaneSmith"));
		assertFalse(Database.get().users().isAvailable("janesmith"));
		assertFalse(Database.get().users().isAvailable("jAnEsMiTh"));
	}

	@Test
	public void shouldCheckeMailValidation() {
		assertTrue(Tools.checkEmail("john@gmx.com"));
		assertTrue(Tools.checkEmail("john.smith@students.unibe.ch"));
		assertFalse(Tools.checkEmail("john@gmx.c"));
		assertFalse(Tools.checkEmail("john@info.museum"));
		assertFalse(Tools.checkEmail("john@...com"));
	}

	@Test
	public void checkMailAssertion() {
		User user = new User("Bill", "bill", "bill@bill.com");
		user.setEmail("bill@aol.com");
		assertEquals(user.getEmail(), "bill@aol.com");
	}

	@Test
	public void checkPassw() {
		User user = new User("Bill", "bill", "bill@bill.com");
		assertTrue(user.checkPW("bill"));
		assertEquals(Tools.encrypt("bill"), user.getSHA1Password());
		assertEquals(Tools.encrypt(""),
				"da39a3ee5e6b4b0d3255bfef95601890afd80709"); // Source:

		// wikipedia.org/wiki/Examples_of_SHA_digests
		assertFalse(Tools.encrypt("password").equals(Tools.encrypt("Password")));

	}

	@Test
	public void shouldEditProfileCorrectly() throws ParseException {
		User user = new User("Jack", "jack", "jack@jack.com");
		user.setDateOfBirth("14.9.1987");
		user.setBiography("I lived");
		user.setEmail("test@test.tt");
		user.setEmployer("TestInc");
		user.setFullname("Test Tester");
		user.setProfession("tester");
		user.setWebsite("http://www.test.ch");

		assertEquals(user.getAge(), 23);
		assertTrue(user.getBiography().equalsIgnoreCase("I lived"));
		assertTrue(user.getEmail().equals("test@test.tt"));
		assertTrue(user.getEmployer().equals("TestInc"));
		assertTrue(user.getFullname().equals("Test Tester"));
		assertTrue(user.getProfession().equals("tester"));
		assertTrue(user.getWebsite().equals("http://www.test.ch"));
	}

	@Test
	public void checkForSpammer() {
		User user = new User("Spammer", "spammer", "spammer@spam.com");
		assertFalse(user.isBlocked());
		assertEquals(user.getStatusMessage(), "");
		assertTrue(user.howManyItemsPerHour() == 0);
		new Question(user, "Why did the chicken cross the road?");
		assertTrue(user.howManyItemsPerHour() == 1);
		new Question(user, "Does anybody know?");
		assertFalse(user.howManyItemsPerHour() == 1);
		for (int i = 0; i < 57; i++) {
			new Question(user, "This is my " + i + ". question");
		}
		assertTrue(!user.isSpammer());
		assertTrue(user.howManyItemsPerHour() == 59);
		assertTrue(!user.isCheating());
		new Question(user, "My last possible Post");
		assertTrue(user.isSpammer());
		assertTrue(user.isCheating());
		assertEquals(user.getStatusMessage(), "User is a Spammer");
		assertTrue(user.isBlocked());
	}

	@Test
	public void checkForCheater() {
		User user = new User("TheSupported", "supported", "support@help.com");
		User user2 = new User("Cheater", "cheater", "cheat@cheater.com");
		assertFalse(user.isBlocked());
		assertFalse(user2.isBlocked());
		assertEquals(user.getStatusMessage(), "");
		assertEquals(user2.getStatusMessage(), "");
		for (int i = 0; i < 5; i++) {
			new Question(user, "This is my " + i + ". question").voteUp(user2);
		}
		assertTrue(user2.isMaybeCheater());
		assertTrue(user2.isCheating());
		assertTrue(user2.isBlocked());
		assertEquals(user2.getStatusMessage(), "User voted up somebody");
		assertFalse(user.isMaybeCheater());
		assertFalse(user.isCheating());
		assertFalse(user.isBlocked());
		assertEquals(user.getStatusMessage(), "");
	}
	
	@Test
	public void shouldNotBeAbleToEditForeignPosts() {
		User user1 = new User("Jack", "jack", "jack@jack.com");
		User user2 = new User("John", "john", "john@john.com");
		User user3 = new User("Geronimo", "geronimo", "geronimo@geronimo.go");
		user1.setModerator(true);
		Question q = new Question(user2, "Can you edit this post?");
		/* moderator should be able to edit the question */
		assertTrue(user1.canEdit(q));
		/* owner should be able to edit the question */
		assertTrue(user2.canEdit(q));
		/* user that is neither a moderator nor the owner of
		   the question should NOT be able to edit the question */
		assertFalse(user3.canEdit(q));
	}

	@Test
	public void shouldHaveOneQuestion() {
		User user = new User("Jack", "jack", "jack@jack.com");
		Question q = new Question(user, "Why?");
		assertEquals(1, user.getQuestions().size());
		q.unregister();
	}

	@Test
	public void shouldHaveNoQuestion() {
		User user = new User("Jack", "jack", "jack@jack.com");
		Question q = new Question(user, "Why?");
		q.unregister();
		assertEquals(0, user.getQuestions().size());
	}

	@Test
	public void shouldHaveOneAnswer() {
		User user = new User("Jack", "jack", "jack@jack.com");
		Question q = new Question(user, "Why?");
		q.answer(user, "Because");
		assertEquals(1, user.getAnswers().size());
	}

	@Test
	public void shouldHaveNoAnswer() {
		User user = new User("Jack", "jack", "jack@jack.com");
		Question q = new Question(user, "Why?");
		q.answer(user, "Because");
		q.answers().get(0).unregister();
		assertEquals(0, user.getAnswers().size());
	}

	@Test
	public void shouldHaveOneBestAnswer() {
		User user = new User("Jack", "jack", "jack@jack.com");
		Question q = new Question(user, "Why?");
		q.answer(user, "Because");
		q.setBestAnswer(q.answers().get(0));
		assertEquals(1, user.bestAnswers().size());
	}

	@Test
	public void shouldHaveNoBestAnswer() {
		User user = new User("Jack", "jack", "jack@jack.com");
		Question q = new Question(user, "Why?");
		q.answer(user, "Because");
		q.setBestAnswer(q.answers().get(0));
		q.answers().get(0).unregister();
		assertEquals(0, user.bestAnswers().size());
	}

	@Test
	public void testModerator() {
		User user = new User("Jack", "jack", "jack@jack.com");
		assertFalse(user.isModerator());
		user.setModerator(true);
		assertTrue(user.isModerator());
	}

	@Test
	public void testBlock() {
		User user = new User("Jack", "jack", "jack@jack.com");
		assertFalse(user.isBlocked());
		assertEquals(user.getStatusMessage(), "");
		user.block("offending comments");
		assertTrue(user.isBlocked());
		assertEquals(user.getStatusMessage(), "offending comments");
		user.unblock();
		assertFalse(user.isBlocked());
		assertEquals(user.getStatusMessage(), "");

	}

	@Test
	public void shouldHaveOneHighRatedAnswer() {
		User user = new User("Jack", "jack", "jack@jack.com");
		Question q = new Question(user, "Why?");
		q.answer(user, "Because");

		User A = new User("A", "a", "a@a.com");
		User B = new User("B", "b", "b@b.com");
		User C = new User("C", "c", "c@c.com");
		User D = new User("D", "d", "d@d.com");
		User E = new User("E", "e", "e@e.com");

		q.answers().get(0).voteUp(A);
		q.answers().get(0).voteUp(B);
		q.answers().get(0).voteUp(C);
		q.answers().get(0).voteUp(D);
		q.answers().get(0).voteUp(E);

		assertEquals(1, user.highRatedAnswers().size());

		A.delete();
		B.delete();
		C.delete();
		D.delete();
		E.delete();

		assertEquals(0, user.highRatedAnswers().size());
	}

	@Test
	public void shouldSuggestQuestion() {
		User user3 = new User("User3", "user3", "user3@user.com");
		User user4 = new User("User4", "user4", "user4@user.com");
		User user5 = new User("User5", "user5", "user5@user.com");
		Question m = new Question(user3, "Why?");
		Question n = new Question(user4, "Where?");

		m.setTagString("demo");
		n.setTagString("demo demo2");
		m.answer(user3, "Because");
		m.answer(user4, "No idea");
		n.answer(user5, "Therefore");

		assertEquals(1, user5.getSuggestedQuestions().size());
		assertEquals(m, user5.getSuggestedQuestions().get(0));

	}

	@Test
	public void shouldSuggestThreeQuestions() {
		User user3 = new User("User3", "user3", "user3@user.com");
		User user4 = new User("User4", "user4", "user4@user.com");
		User user5 = new User("User5", "user5", "user5@user.com");
		Question m = new Question(user3, "Why?");
		Question n = new Question(user4, "Where?");
		Question o = new Question(user3, "Who?");
		Question p = new Question(user4, "How old?");

		m.setTagString("demo");
		n.setTagString("demo demo2");
		o.setTagString("demo demo3 demo4");
		p.setTagString("demo demo3 demo4 demo5");
		m.answer(user3, "Because");
		m.answer(user4, "No idea");
		n.answer(user5, "Therefore");

		assertEquals(3, user5.getSuggestedQuestions().size());
		assertEquals(m, user5.getSuggestedQuestions().get(0));
	}

	@Test
	public void shouldSuggestQuestionsFromBestAnswersFirst() {
		User user3 = new User("User3", "user3", "user3@user.com");
		User user4 = new User("User4", "user4", "user4@user.com");
		User user5 = new User("User5", "user5", "user5@user.com");
		Question m = new Question(user3, "Why?");
		Question n = new Question(user4, "Where?");
		Question o = new Question(user3, "Who?");
		Question p = new Question(user4, "How old?");

		m.setTagString("demo");
		n.setTagString("demo demo2");
		o.setTagString("demo9 demo8");
		p.setTagString("demo9 demo8");
		m.answer(user3, "Because");
		m.answer(user4, "No idea");
		n.answer(user5, "Therefore");
		o.answer(user5, "No");
		o.setBestAnswer(user5.getAnswers().get(1));
		assertEquals(2, user5.getSuggestedQuestions().size());
		assertEquals(p, user5.getSuggestedQuestions().get(0));
		assertEquals(m, user5.getSuggestedQuestions().get(1));

	}

	@Test
	public void shouldSuggestQuestionsSortedByRatingOfAnswers() {
		User user3 = new User("User3", "user3", "user3@user.com");
		User user4 = new User("User4", "user4", "user4@user.com");
		User user5 = new User("User5", "user5", "user5@user.com");
		Question m = new Question(user3, "Why?");
		Question n = new Question(user4, "Where?");
		Question o = new Question(user3, "Who?");
		Question p = new Question(user4, "How old?");
		Question q = new Question(user3, "So?");
		Question r = new Question(user4, "For ho long?");

		m.setTagString("demo");
		n.setTagString("demo demo2");
		o.setTagString("demo9 demo8");
		p.setTagString("demo9 demo8");
		q.setTagString("demo demo2 demo10");
		r.setTagString("tag");
		m.answer(user3, "Because");
		m.answer(user4, "No idea");
		n.answer(user5, "Therefore");
		o.answer(user5, "No");
		user5.getAnswers().get(1).voteUp(user3);
		user5.getAnswers().get(1).voteUp(user4);

		assertEquals(3, user5.getSuggestedQuestions().size());
		assertEquals(q, user5.getSuggestedQuestions().get(0));
		assertEquals(m, user5.getSuggestedQuestions().get(1));
		assertEquals(p, user5.getSuggestedQuestions().get(2));

	}

	@Test
	public void shouldNotSuggestQuestionsFromBadAnswers() {
		User user6 = new User("User6", "user6", "user6@user.com");
		User user7 = new User("User7", "user7", "user7@user.com");
		User user8 = new User("User8", "user8", "user8@user.com");
		Question m = new Question(user6, "Why?");
		Question n = new Question(user7, "Where?");
		Question o = new Question(user6, "Who?");
		Question p = new Question(user7, "How old?");
		m.answer(user6, "Because");
		m.answer(user7, "No idea");
		p.answer(user8, "Therefore");
		user8.getAnswers().get(0).voteDown(user6);

		m.setTagString("demo");
		n.setTagString("demo demo2");
		o.setTagString("demo demo3 demo4");
		p.setTagString("demo demo3 demo4 demo5");

		assertEquals(0, user8.getSuggestedQuestions().size());
	}

	@Test
	public void shouldNotSuggestOwnQuestions() {
		User user = new User("Jack", "jack", "jack@jack.com");
		User user2 = new User("John", "john", "john@john.com");
		Question q = new Question(user, "Why?");
		Question f = new Question(user2, "Where?");
		q.setTagString("demo");
		f.setTagString("demo");
		q.answer(user2, "Because");
		assertEquals(0, user2.getSuggestedQuestions().size());

	}

	@Test
	public void shouldNotSuggestQuestionsWithBestAnswer() {
		User james = new User("James", "james", "james@james.com");
		User john = new User("John", "john", "john@john.com");
		User kate = new User("Kate", "kate", "kate@kate.com");
		Question k = new Question(james, "Why?");
		Question l = new Question(john, "Where?");

		k.setTagString("demo");
		l.setTagString("demo");
		k.answer(james, "Because");
		k.answer(john, "No idea");
		james.getQuestions().get(0).setBestAnswer(k.getAnswer(1));
		l.answer(kate, "Therefore");
		assertEquals(0, kate.getSuggestedQuestions().size());

	}

}
