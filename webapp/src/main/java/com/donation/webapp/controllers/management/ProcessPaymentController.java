package com.donation.webapp.controllers.management;

import com.donation.common.core.exception.InvalidMessageException;
import com.donation.management.dto.ManualPaymentDto;
import com.donation.data.handler.service.ManualPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static com.donation.common.core.constant.UrlMapping.ADMIN_PAYMENT;

@Controller
@RequestMapping(ADMIN_PAYMENT)
public class ProcessPaymentController {
    @Autowired
    private ManualPaymentService manualPaymentService;


    @GetMapping
    public String showPayment(Model theModel) {

        return "views/admin/payment/admin_payment";
    }

    @PostMapping("/update")
    public String updatePaymentTransfer(ManualPaymentDto manualPaymentDto,
                                        RedirectAttributes redirectAttributes) {

        try {
            Boolean isUpdated = manualPaymentService.updateTransferInformation(manualPaymentDto);

            if (isUpdated) {
                redirectAttributes.addFlashAttribute("message", "Transfer has been updated");
            } else {
                redirectAttributes.addFlashAttribute("error", "Transfer cannot execute");
            }

        } catch (InvalidMessageException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:" + ADMIN_PAYMENT;
    }

    @PostMapping("/scan")
    public String scanBankStatement(@RequestParam("file-upload") MultipartFile multipartFile,
                                    Model theModel) throws InvalidMessageException {

        if (!multipartFile.isEmpty()) {
            manualPaymentService.scanFile(multipartFile);
        }

        theModel.addAttribute("scanMessage", "Complete scan file");
        return "views/admin/payment/admin_payment";
    }
}
