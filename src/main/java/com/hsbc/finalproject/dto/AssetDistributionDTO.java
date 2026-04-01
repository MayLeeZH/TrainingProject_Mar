package com.hsbc.finalproject.dto;

import com.hsbc.finalproject.model.enums.AssetType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumeratedValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssetDistributionDTO {
    @Enumerated(EnumType.STRING)
    private AssetType assetType;

    private Double amount;

    private Double proportion;

}
