"""
Base model configuration for Seqera SDK.
"""

from pydantic import BaseModel, ConfigDict


class SeqeraModel(BaseModel):
    """
    Base model for all Seqera API entities.

    Configured with:
    - extra="allow": Accept unknown fields from API responses
    - populate_by_name=True: Support both snake_case and camelCase
    - use_enum_values=True: Serialize enums to their values
    """

    model_config = ConfigDict(
        extra="allow",
        populate_by_name=True,
        use_enum_values=True,
    )
