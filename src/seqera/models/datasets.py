"""
Dataset models for Seqera SDK.
"""

from datetime import datetime

from pydantic import Field

from seqera.models.base import SeqeraModel


class Dataset(SeqeraModel):
    """
    Dataset entity from the Seqera Platform.

    Datasets are versioned data files that can be used as inputs to pipelines.
    """

    id: str
    name: str
    description: str | None = None
    media_type: str | None = Field(None, alias="mediaType")
    deleted: bool = False
    date_created: datetime | None = Field(None, alias="dateCreated")
    last_updated: datetime | None = Field(None, alias="lastUpdated")


class DatasetVersion(SeqeraModel):
    """
    Dataset version entity from the Seqera Platform.

    Each dataset can have multiple versions representing different
    snapshots of the data.
    """

    dataset_id: str = Field(alias="datasetId")
    version: int
    file_name: str | None = Field(None, alias="fileName")
    media_type: str | None = Field(None, alias="mediaType")
    url: str | None = None
    has_header: bool = Field(False, alias="hasHeader")
    date_created: datetime | None = Field(None, alias="dateCreated")
    last_updated: datetime | None = Field(None, alias="lastUpdated")
