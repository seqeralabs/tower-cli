"""
Common models shared across the SDK.
"""

from typing import Callable, Generic, Iterator, TypeVar

from seqera.models.base import SeqeraModel

T = TypeVar("T", bound=SeqeraModel)


class PaginatedList(Generic[T]):
    """
    Auto-paginating iterator for list operations.

    Automatically fetches additional pages from the API as you iterate.

    Example:
        >>> for pipeline in client.pipelines.list():
        ...     print(pipeline.name)

        >>> # Or collect all items
        >>> all_pipelines = list(client.pipelines.list())
    """

    def __init__(
        self,
        fetch_page: Callable[[int, int], tuple[list[T], int]],
        page_size: int = 50,
        initial_items: list[T] | None = None,
        total_size: int | None = None,
    ) -> None:
        """
        Initialize paginated list.

        Args:
            fetch_page: Function that takes (offset, limit) and returns (items, total_size)
            page_size: Number of items per page
            initial_items: Items from the first page (if already fetched)
            total_size: Total count of items (if known)
        """
        self._fetch_page = fetch_page
        self._page_size = page_size
        self._items: list[T] = initial_items or []
        self._total_size = total_size
        self._offset = len(self._items)
        self._exhausted = False
        self._fetched_first = initial_items is not None

    def __iter__(self) -> Iterator[T]:
        """Return an iterator over all items."""
        return PaginatedListIterator(self)

    def __len__(self) -> int:
        """Return total count of items (fetches first page if needed)."""
        if self._total_size is None:
            self._ensure_first_page()
        return self._total_size or 0

    @property
    def total_size(self) -> int:
        """Total number of items available (fetches first page if needed)."""
        if self._total_size is None:
            self._ensure_first_page()
        return self._total_size or 0

    def _ensure_first_page(self) -> None:
        """Ensure the first page has been fetched."""
        if not self._fetched_first:
            self._fetch_next_page()

    def _fetch_next_page(self) -> bool:
        """
        Fetch the next page of items.

        Returns:
            True if more items were fetched, False if exhausted
        """
        if self._exhausted:
            return False

        items, total_size = self._fetch_page(self._offset, self._page_size)
        self._total_size = total_size
        self._fetched_first = True

        if not items:
            self._exhausted = True
            return False

        self._items.extend(items)
        self._offset += len(items)

        # Check if we've fetched all items
        if self._offset >= total_size:
            self._exhausted = True

        return True


class PaginatedListIterator(Generic[T]):
    """Iterator for PaginatedList that fetches pages on demand."""

    def __init__(self, paginated_list: PaginatedList[T]) -> None:
        self._list = paginated_list
        self._index = 0

    def __iter__(self) -> "PaginatedListIterator[T]":
        return self

    def __next__(self) -> T:
        # Try to get item at current index
        while self._index >= len(self._list._items):
            # Need to fetch more items
            if self._list._exhausted:
                raise StopIteration
            if not self._list._fetch_next_page():
                raise StopIteration

        item = self._list._items[self._index]
        self._index += 1
        return item
