package se.uhr.simone.atom.feed.server.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.UUID;

import javax.sql.DataSource;
import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

@ExtendWith(MockitoExtension.class)
public class FeedRepositoryTest {

	@Mock
	private AtomFeedDAO atomFeedDAO;

	@Mock
	private AtomEntryDAO atomEntryDAO;

	@Mock
	private AtomCategoryDAO atomCategoryDAO;

	@Mock
	private AtomLinkDAO atomLinkDAO;

	@Mock
	private AtomAuthorDAO atomAuthorDAO;

	@InjectMocks
	private FeedRepository feedRepository = new TestableFeedRepository();

	@Test
	public void saveAtomFeedWithoutEntries() {
		AtomFeed atomFeed = new AtomFeed(1);

		given(atomFeedDAO.exists(1)).willReturn(true);

		feedRepository.saveAtomFeed(atomFeed);

		verify(atomFeedDAO, times(1)).exists(1);
		verify(atomFeedDAO, times(1)).update(atomFeed);
		verify(atomEntryDAO, never()).exists(any(String.class));
	}

	@Test
	public void saveAtomFeedWithEntries() {
		AtomFeed atomFeed = new AtomFeed(1);
		AtomEntry atomEntry = createAtomEntry();

		atomFeed.getEntries().add(atomEntry);

		given(atomFeedDAO.exists(1)).willReturn(false);
		given(atomEntryDAO.exists(atomEntry.getAtomEntryId())).willReturn(false);

		feedRepository.saveAtomFeed(atomFeed);

		verify(atomFeedDAO, times(1)).exists(1);
		verify(atomFeedDAO, times(1)).insert(atomFeed);
		verify(atomEntryDAO, times(1)).exists(atomEntry.getAtomEntryId());
		verify(atomEntryDAO, times(1)).insert(atomEntry);
	}

	@Test
	public void saveAtomEntryWithoutCategories() {
		AtomEntry atomEntry = createAtomEntry();

		given(atomEntryDAO.exists(atomEntry.getAtomEntryId())).willReturn(true);

		feedRepository.saveAtomEntry(atomEntry);

		verify(atomEntryDAO, times(1)).exists(atomEntry.getAtomEntryId());
		verify(atomEntryDAO, times(1)).update(atomEntry);
		verify(atomLinkDAO, times(1)).delete(atomEntry.getAtomEntryId());
		verify(atomAuthorDAO, times(1)).delete(atomEntry.getAtomEntryId());
		//		verify(atomCategoryDAO, never()).exists(any(Long.class));
		verify(atomCategoryDAO, never()).isConnected(any(AtomCategory.class), eq(atomEntry.getAtomEntryId()));
	}

	@Test
	public void saveAtomEntryWithCategories() {

	}

	@Test
	public void getFeedByIdNotExisting() {

		given(atomFeedDAO.fetchBy(1)).willThrow(new EmptyResultDataAccessException(3));

		AtomFeed feed = feedRepository.getFeedById(1);
		assertThat(feed).isNull();

		verify(atomFeedDAO, times(1)).fetchBy(1);
		verify(atomEntryDAO, never()).getAtomEntriesForFeed(1);
	}

	@Test
	public void getFeedById() {
		AtomFeed atomFeed = new AtomFeed(1);

		given(atomFeedDAO.fetchBy(1)).willReturn(atomFeed);
		given(atomEntryDAO.getAtomEntriesForFeed(1)).willReturn(Arrays.asList(createAtomEntry()));

		AtomFeed fetchedFeed = feedRepository.getFeedById(1);

		assertThat(fetchedFeed).isNotNull();
		assertThat(fetchedFeed.getEntries()).hasSize(1);

		verify(atomFeedDAO, times(1)).fetchBy(1);
		verify(atomEntryDAO, times(1)).getAtomEntriesForFeed(1);
	}

	@Test
	public void getRecentFeedMustExistInDatabase() {
		given(atomFeedDAO.fetchRecent()).willThrow(new EmptyResultDataAccessException(3));

		assertThatExceptionOfType(EmptyResultDataAccessException.class).isThrownBy(() -> {
			feedRepository.getRecentFeed();
		});

	}

	@Test
	public void getRecentFeed() {
		AtomFeed atomFeed = new AtomFeed(1);

		given(atomFeedDAO.fetchRecent()).willReturn(atomFeed);
		given(atomEntryDAO.getAtomEntriesForFeed(1)).willReturn(Arrays.asList(createAtomEntry()));

		AtomFeed fetchedFeed = feedRepository.getRecentFeed();

		assertThat(fetchedFeed).isNotNull();
		assertThat(fetchedFeed.getEntries()).hasSize(1);

		verify(atomFeedDAO, times(1)).fetchRecent();
		verify(atomEntryDAO, times(1)).getAtomEntriesForFeed(1);
	}

	private AtomEntry createAtomEntry() {
		return AtomEntry.builder()
				.withAtomEntryId(UUID.randomUUID().toString())
				.withSortOrder(Long.valueOf(1))
				.withSubmittedNow()
				.withContent(Content.builder().withValue("<xml></xml>").withContentType(MediaType.APPLICATION_XML).build())
				.build();
	}

	private class TestableFeedRepository extends FeedRepository {

		@Override
		public DataSource getDataSource() {
			return null;
		}
	}
}
