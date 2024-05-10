package com.example.contactapi.servise;

import com.example.contactapi.domain.Contact;
import com.example.contactapi.repo.ContactRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.example.contactapi.constatnt.Constant.PHOTO_DIRECTORY;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
@Slf4j
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepository contactRepository;

	public Page<Contact> getAllContacts(int page, int size) {
		return contactRepository.findAll(PageRequest.of(page, size, Sort.by("name")));
	}
	public Contact getContact(String id) {
		return contactRepository.findById(id).orElseThrow(() ->new RuntimeException("Contact not found"));
	}
	public Contact createContact(Contact contact) {
		return contactRepository.save(contact);
	}
	public void deleteContact(Contact contact) {
		contactRepository.delete(contact);
	}
	public String uploadThePhoto(String id, MultipartFile file) {
		Contact contact = getContact(id);
		String photoUrl = null;
		contact.setPhotoUrl(photoUrl);
		contactRepository.save(contact);
		return photoUrl;
	}

	private final Function<String, String> fileExtetion = filename -> Optional.of(filename).filter(name ->name.contains("."))
			.map(name -> "." +  name.substring(filename.lastIndexOf(".") + 1)).orElse(".png");

	private final BiFunction<String, MultipartFile, String> photoMethod = (id, image) ->{
		String filename = id + fileExtetion.apply(image.getOriginalFilename());
		try{
			Path fileStorageLocation = Paths.get(PHOTO_DIRECTORY).toAbsolutePath().normalize();
			if (!Files.exists(fileStorageLocation)) {
				Files.createDirectories(fileStorageLocation);
			}
			Files.copy(image.getInputStream(), fileStorageLocation.resolve(filename), REPLACE_EXISTING);
			return ServletUriComponentsBuilder
					.fromCurrentContextPath()
					.path("/contacts/image/" + filename).toUriString();
		} catch (Exception e) {
			throw new RuntimeException("Unable to save image");
		}
	};
}
