package com.octl2.api.service.impl;

import com.octl2.api.dto.ProvinceDTO;
import com.octl2.api.repository.ProvinceRepository;
import com.octl2.api.service.ProvinceService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProvinceServiceImpl implements ProvinceService {

    ProvinceRepository provinceRepo;

    @Override
    public ProvinceDTO getBybId(long id) {
        return provinceRepo.getDtoById(id);
    }
}
