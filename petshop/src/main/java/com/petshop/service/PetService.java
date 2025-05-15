package com.petshop.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.petshop.entity.Pet;
import com.petshop.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PetService extends IService<Pet> {

    // 正确方法（增加分页参数）
    Page<Pet> getPets(String category, String name, int page, int size);

    //删除
    void deletePetWithValidation(Integer id) throws BusinessException;

    Pet  updatePet(Integer id, Pet pet, MultipartFile imageFile) throws IOException;

    Page<Pet> getPetsBySeller(Integer userId, String category, String name, int page, int size);

    void batchDeleteWithValidation(List<Integer> ids);
}