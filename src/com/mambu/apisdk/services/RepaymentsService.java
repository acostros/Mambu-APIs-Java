/**
 * 
 */
package com.mambu.apisdk.services;

import java.util.List;

import com.google.inject.Inject;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.ServiceHelper;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.Repayment;

/**
 * Service class which handles API operations like entering and getting repayments
 * 
 * @author ipenciuc
 * 
 */
public class RepaymentsService {

	private static final String DUE_FROM = APIData.DUE_FROM;
	private static final String DUE_TO = APIData.DUE_TO;

	private static final String OFFSET = APIData.OFFSET;
	private static final String LIMIT = APIData.LIMIT;

	private ServiceHelper serviceHelper;

	// Create API definitions for services provided by ClientService

	private final static ApiDefinition getRepaymments = new ApiDefinition(ApiType.GET_LIST, Repayment.class);
	private final static ApiDefinition getRepaymentsForLoan = new ApiDefinition(ApiType.GET_OWNED_ENTITIES,
			LoanAccount.class, Repayment.class);

	/***
	 * Create a new repayments service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public RepaymentsService(MambuAPIService mambuAPIService) {
		this.serviceHelper = new ServiceHelper(mambuAPIService);
	}

	/***
	 * Get a all Repayments by Loan account id
	 * 
	 * @param accountId
	 *            the id of the loan account
	 * 
	 * @return the List of Repayments
	 * 
	 * @throws MambuApiException
	 */
	public List<Repayment> getLoanAccountRepayments(String accountId) throws MambuApiException {
		return serviceHelper.execute(getRepaymentsForLoan, accountId);
	}

	/***
	 * Get a loan account Repayments between FromDate and ToDate
	 * 
	 * @param dueFomString
	 * @param dueToString
	 * 
	 * @return the List of Repayments
	 * 
	 * @throws MambuApiException
	 */
	public List<Repayment> getRapaymentsDueFromTo(String dueFromString, String dueToString) throws MambuApiException {
		// E.g. GET /api/repayments?dueFrom=2011-01-05&dueTo=2011-06-07
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.put(DUE_FROM, dueFromString);
		paramsMap.put(DUE_TO, dueToString);

		return serviceHelper.execute(getRepaymments, paramsMap);
	}

	/***
	 * Get a all Repayments by Loan account id with an offset and limit params
	 * 
	 * @param accountId
	 *            the id of the loan account limit - last transaction number Note: if offset and limit both equal null,
	 *            all transactions are returned
	 * 
	 * @return the List of Repayments
	 * 
	 * @throws MambuApiException
	 */
	// TODO: The offset and limit params are not supported by repayments. API returns all. To be investigated further
	public List<Repayment> getLoanAccountRepayments(String accountId, String offset, String limit)
			throws MambuApiException {

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.put(OFFSET, offset);
		paramsMap.put(LIMIT, limit);
		return serviceHelper.execute(getRepaymentsForLoan, accountId, paramsMap);
	}

}
