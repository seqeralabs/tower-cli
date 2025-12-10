"""
Label models for Seqera SDK.
"""

from pydantic import Field

from seqera.models.base import SeqeraModel


class Label(SeqeraModel):
    """
    Label entity from the Seqera Platform.

    Labels are key-value pairs used to organize and filter resources.
    """

    id: int
    name: str
    value: str | None = None
    resource: bool = False
    is_default: bool = Field(False, alias="isDefault")
