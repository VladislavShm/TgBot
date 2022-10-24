package com.tgbot.service;

import com.tgbot.entity.Royalty;
import com.tgbot.entity.TgUser;
import com.tgbot.model.internal.telegram.Text;
import com.tgbot.repository.RoyaltyRepository;
import com.tgbot.utils.TonCoinUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;

@Service
@RequiredArgsConstructor
public class RoyaltyService {

    private final NftService nftService;
    private final RoyaltyRepository royaltyRepository;
    private final TransactionService transactionService;

    public Text createInfoMessage(TgUser user) {
        if (StringUtils.isEmpty(user.getWallet())) {
            return new Text("base_location.royalty.wallet_not_specified");
        }

        String nextInterestDate = royaltyRepository.findFirstByOrderByDateTimeDesc()
                .map(Royalty::getDateTime)
                .or(() -> Optional.of(LocalDateTime.now()))
                .map(datetime -> datetime.plusMonths(1).with(firstDayOfMonth()))
                .map(LocalDateTime::toLocalDate)
                .map(date ->
                        date.format(
                                DateTimeFormatter.ofPattern(
                                        "dd MMM yyyy",
                                        ObjectUtils.defaultIfNull(user.getLocale(), Locale.forLanguageTag("ru"))
                                )
                        )
                )
                .orElseThrow();

        Integer userNftCount = nftService.findUserNftCount(user);
        BigInteger sumOfRoyalties = transactionService.sumOfRoyalties();
        Integer totalNftNumberWithOwner = nftService.totalNftNumberWithOwner();
        BigInteger paidRoyaltySum = ObjectUtils.defaultIfNull(royaltyRepository.royaltySum(), BigInteger.ZERO);
        BigInteger userRoyaltySum = ObjectUtils.defaultIfNull(royaltyRepository.royaltySum(user.getWallet()), BigInteger.ZERO);
        return new Text("base_location.royalty.info")
                .param(TonCoinUtils.toHumanReadable(paidRoyaltySum))
                .param(TonCoinUtils.toHumanReadable(userRoyaltySum))
                .param(nextInterestDate)
                .param(TonCoinUtils.toHumanReadable(
                        sumOfRoyalties
                                .subtract(
                                        paidRoyaltySum
                                )
                                .divide(
                                        BigInteger.valueOf(totalNftNumberWithOwner)
                                ).multiply(
                                        BigInteger.valueOf(userNftCount)
                                )
                ));
    }
}
