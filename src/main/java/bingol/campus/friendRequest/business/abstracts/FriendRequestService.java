package bingol.campus.friendRequest.business.abstracts;

import bingol.campus.friendRequest.core.exceptions.*;

import bingol.campus.friendRequest.core.response.ReceivedFriendRequestDTO;
import bingol.campus.friendRequest.core.response.SentFriendRequestDTO;
import bingol.campus.response.DataResponseMessage;
import bingol.campus.response.ResponseMessage;
import bingol.campus.student.exceptions.StudentNotFoundException;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FriendRequestService {
    ResponseMessage sendFriendRequest(String username, Long userId) throws StudentNotFoundException, SelfFriendRequestException, AlreadySentRequestException, AlreadyFollowingException, BlockedByUserException, UserBlockedException;
    ResponseMessage acceptFriendRequest(String username, Long requestId) throws AlreadyAcceptedRequestException, FriendRequestNotFoundException, StudentNotFoundException, UnauthorizedRequestException;

    ResponseMessage rejectFriendRequest(String username, Long requestId) throws AlreadyRejectedRequestException, FriendRequestNotFoundException, StudentNotFoundException;

    DataResponseMessage getFriendRequestById(String username, Long requestId) throws UnauthorizedRequestException, FriendRequestNotFoundException, StudentNotFoundException;

    ResponseMessage cancelFriendRequest(String username, Long requestId) throws FriendRequestNotFoundException, UnauthorizedRequestException, StudentNotFoundException;



    ResponseMessage acceptFriendRequestsBulk(String username, List<Long> requestIds) throws StudentNotFoundException;

    ResponseMessage rejectFriendRequestsBulk(String username, List<Long> requestIds) throws StudentNotFoundException;

    DataResponseMessage<List<ReceivedFriendRequestDTO>> getReceivedFriendRequests(String username, Pageable pageable) throws StudentNotFoundException;

    DataResponseMessage<List<SentFriendRequestDTO>> getSentFriendRequests(String username, Pageable pageable) throws StudentNotFoundException;
}
