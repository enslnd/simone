package se.uhr.simone.atom.feed.server.entity;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.joda.time.DateTimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.jdbc.core.JdbcTemplate;

import se.uhr.simone.atom.feed.server.entity.AtomEntry.AtomEntryId;
import se.uhr.simone.atom.feed.utils.UniqueIdentifier;

@ExtendWith(DataSourceParameterResolver.class)
public class AtomAuthorDAOTest {

	private static final Person AUTHOR = Person.of("AUTHOR TEST");

	private AtomAuthorDAO atomAuthorDAO;

	private AtomEntryId atomEntryId = createNewAtomEntryId();

	@BeforeEach
	public void setup(DataSource ds) {
		atomAuthorDAO = new AtomAuthorDAO(new JdbcTemplate(ds));
		AtomEntryDAO atomEntryDAO = new AtomEntryDAO(new JdbcTemplate(ds));
		atomEntryDAO.insert(AtomEntry.builder().withAtomEntryId(atomEntryId).withSortOrder(1L).withSubmittedNow().build());
		DateTimeUtils.setCurrentMillisSystem();
	}

	@Test
	public void notExists() {
		assertThat(atomAuthorDAO.exists(atomEntryId)).isFalse();
	}

	@Test
	public void exists() {
		atomAuthorDAO.insert(atomEntryId, AUTHOR);
		assertThat(atomAuthorDAO.exists(atomEntryId)).isTrue();
	}

	@Test
	public void insert() {
		atomAuthorDAO.insert(atomEntryId, AUTHOR);
	}

	@Test
	public void emptyListWhenNoResult() {
		assertThat(atomAuthorDAO.findBy(AtomEntryId.of(UniqueIdentifier.randomUniqueIdentifier().getValue(), "non-existing")))
				.isEmpty();
	}

	@Test
	public void findsAllAuthorsForEntryId() {

		atomAuthorDAO.insert(atomEntryId, AUTHOR);
		atomAuthorDAO.insert(atomEntryId, AUTHOR);
		assertThat(atomAuthorDAO.findBy(atomEntryId)).hasSize(2);
	}

	@Test
	public void deleteAllAuthorsForEntry() {
		atomAuthorDAO.insert(atomEntryId, AUTHOR);
		atomAuthorDAO.delete(atomEntryId);
		assertThat(atomAuthorDAO.findBy(atomEntryId)).isEmpty();
	}

	private AtomEntryId createNewAtomEntryId() {
		return AtomEntryId.of(UniqueIdentifier.randomUniqueIdentifier().getValue(), "application/xml");
	}
}
