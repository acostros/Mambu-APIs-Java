package com.mambu.apisdk.util;

import java.util.HashMap;
import java.util.Map;

import com.mambu.accounting.shared.model.GLAccount;
import com.mambu.accounting.shared.model.GLJournalEntry;
import com.mambu.accounts.shared.model.TransactionChannel;
import com.mambu.api.server.handler.activityfeed.model.JSONActivity;
import com.mambu.api.server.handler.documents.model.JSONDocument;
import com.mambu.api.server.handler.savings.model.JSONSavingsAccount;
import com.mambu.api.server.handler.tasks.model.JSONTask;
import com.mambu.apisdk.model.LoanAccountExpanded;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.ClientExpanded;
import com.mambu.clients.shared.model.Group;
import com.mambu.clients.shared.model.GroupExpanded;
import com.mambu.core.shared.model.Currency;
import com.mambu.core.shared.model.CustomField;
import com.mambu.core.shared.model.CustomFieldSet;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.core.shared.model.CustomView;
import com.mambu.core.shared.model.Image;
import com.mambu.core.shared.model.SearchResult;
import com.mambu.core.shared.model.User;
import com.mambu.docs.shared.model.Document;
import com.mambu.intelligence.shared.model.Intelligence.Indicator;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanProduct;
import com.mambu.loans.shared.model.LoanTransaction;
import com.mambu.loans.shared.model.Repayment;
import com.mambu.organization.shared.model.Branch;
import com.mambu.organization.shared.model.Centre;
import com.mambu.savings.shared.model.SavingsAccount;
import com.mambu.savings.shared.model.SavingsProduct;
import com.mambu.savings.shared.model.SavingsTransaction;
import com.mambu.tasks.shared.model.Task;

/**
 * ApiDefinition is a helper class which allows service classes to provide a specification for a Mambu API request and
 * then use ServiceHelper class to actually execute the API request with this ApiDefinition and with provided input
 * parameters. This class allows users to define API format parameters required by a specific API request, including the
 * URL path structure, the HTTP method and content type, and the specification for the expected Mambu response
 * 
 * For the URL path part of the specification, the API definition assumes the URL path to be build in the following
 * format: endpoint[/objectId][/relatedEntity][//[relatedEntityID]], with all parts , except the endpoint, being
 * optional. Examples: /loans, /savings/1234, clients/456/loans, /loans/4556/repayments, /groups/998878,
 * /loans/9876/transactions
 * 
 * For the HTTP part, the ApiDefinition allows users to specify such HTTP parameters as method (GET, POST, DELETE) and
 * the content type
 * 
 * For the expected response, the API definition allows users to specify what is returned by Mambu: the object's class
 * and is it a single object or a collection returned by Mambu for this API request.
 * 
 * ApiDefinition class provides a number of convenience constructors to be used to set the required parameters.
 * 
 * ApiDefinition class also defines a number of typical Mambu API request types (see ApiType). These include standard
 * Mambu API requests, such as Get Entity Details, Get a List of Entities, Create JSON Entity, Get Account Transactions,
 * etc..). Constructor with an ApiType parameter derives the URL format and HTTP parameters from the ApiType, the user
 * typically only needs to additionally specify the Mambu class to be used with this request.
 * 
 * Examples: to define a specification for getting LoanDetails the user can specify a constructor in the following way:
 * new ApiDefinition(ApiType.GetEntityDetails, LoanAccount.class); To provide a specification for Creating New client
 * the user can create definition with new ApiDefinition(ApiType.Create, LoanAccountExpanded.class);
 * 
 * Note, that the Api definition doesn't include any of the input parameters which can be supplied with an API request,
 * this is to be provided as input params map in the service class' calls. The only exception is the ability to specify
 * that the 'fullDetails' parameter is required, in this case the caller doesn't need to provide it as input. For
 * example, when specifying the ApiType.GetEntityDetails type, the fullDetails parameter will be added automatically for
 * this definition.
 * 
 * 
 * @author mdanilkis
 * 
 */
public class ApiDefinition {

	/**
	 * ApiType defines all typical types of the Mambu API requests, such as getting entity details, getting lists and
	 * others.
	 * 
	 */
	// These symbolic names are used in ApiType enum constructors just for constrcutor's readability
	private final static boolean withObjectId = true;
	private final static boolean noObjectId = false;
	private final static boolean hasRelatedEntityPart = true;
	private final static boolean noRelatedEntityPart = false;
	private final static boolean fullDetails = true;
	private final static boolean noFullDetails = false;

	public enum ApiType {

		// Get Entity without Details. Example GET clients/3444
		GET_ENTITY(Method.GET, ContentType.WWW_FORM, withObjectId, noFullDetails, noRelatedEntityPart,
				ApiReturnFormat.OBJECT),
		// Get Entity wit full Details. Example: GET loan/5566?fullDetails=true
		GET_ENTITY_DETAILS(Method.GET, ContentType.WWW_FORM, withObjectId, fullDetails, noRelatedEntityPart,
				ApiReturnFormat.OBJECT),
		// Get a List of Entities. Example: GET savings/
		GET_LIST(Method.GET, ContentType.WWW_FORM, noObjectId, noFullDetails, noRelatedEntityPart,
				ApiReturnFormat.COLLECTION),
		// Get Entities owned by another entity, Example: GET clients/1233/loans or GET loans/233/transactions
		GET_OWNED_ENTITIES(Method.GET, ContentType.WWW_FORM, withObjectId, noFullDetails, hasRelatedEntityPart,
				ApiReturnFormat.COLLECTION),
		// Get Entities related to another entity, For example get transactions of loan type. Example: GET
		// loans/transactions or GET savings/transactions
		GET_RELATED_ENTITIES(Method.GET, ContentType.WWW_FORM, noObjectId, noFullDetails, hasRelatedEntityPart,
				ApiReturnFormat.COLLECTION),
		// Update an entity owned by another entity. Example, update custom field value for a client or group:
		// PATCH clients/client_id/custominformation/custom_field_id
		PATCH_OWNED_ENTITY(Method.PATCH, ContentType.JSON, withObjectId, noFullDetails, hasRelatedEntityPart,
				ApiReturnFormat.BOOLEAN),
		// Delete and an entity owned by another entity. Example, delete custom field for a client:
		// DELETE clients/client_id/custominformation/custom_field_id
		DELETE__OWNED_ENTITY(Method.DELETE, ContentType.WWW_FORM, withObjectId, noFullDetails, hasRelatedEntityPart,
				ApiReturnFormat.BOOLEAN),
		// Create Entity JSON request. Example: POST client/ (contentType=JSON)
		CREATE_JSON_ENTITY(Method.POST, ContentType.JSON, noObjectId, noFullDetails, noRelatedEntityPart,
				ApiReturnFormat.OBJECT),
		// POST Entity using ContentType.WWW_FORM with params map. Used for older APIs versions not using JSON
		CREATE_FORM_ENTITY(Method.POST, ContentType.WWW_FORM, noObjectId, noFullDetails, noRelatedEntityPart,
				ApiReturnFormat.OBJECT),
		// Update Entity JSON request. Example: POST loans/88666 (contentType=JSON)
		UPDATE_JSON(Method.POST, ContentType.JSON, withObjectId, noFullDetails, noRelatedEntityPart,
				ApiReturnFormat.OBJECT),
		// Delete Entity Example: DELETE client/976
		DELETE_ENTITY(Method.DELETE, ContentType.WWW_FORM, withObjectId, noFullDetails, noRelatedEntityPart,
				ApiReturnFormat.BOOLEAN),
		// Post Owned Entity. Example: POST loans/822/transactions?type=REPAYMENT; returns Owned Entity (e.g returns
		// LoanTransaction)
		POST_OWNED_ENTITY(Method.POST, ContentType.WWW_FORM, withObjectId, noFullDetails, hasRelatedEntityPart,
				ApiReturnFormat.OBJECT),
		// Post Entity Change. Example: POST loans/822/transactions?type=APPROVE. Returns Entity object (e.g.
		// LoanAccount)
		POST_ENTITY_ACTION(Method.POST, ContentType.WWW_FORM, withObjectId, noFullDetails, hasRelatedEntityPart,
				ApiReturnFormat.OBJECT);
		/**
		 * Initialise ApiType enum specifying API parameters to be used by this enum value
		 * 
		 * @param method
		 *            HTTP Method to be used by the API request (e.g. Method.GET, Method.POST)
		 * @param contentType
		 *            contentType to be used by the API request (e.g. ContentType.WWW_FORM, ContentType.JSON)
		 * @param requiresObjectId
		 *            a boolean specifying if the request must add object ID to the API request
		 * @param withFullDetails
		 *            a boolean specifying if the request must specify fullDetails parameter
		 * @param requiresRelatedEntity
		 *            a boolean specifying if the request must add the 'relatedEntity' component in the URL path,
		 *            formatted as /endpoint[/objectId][/relatedEntity][/relatedEntityID]
		 * @param relatedEntity
		 *            a string to be used as a 'relatedEntity' part in the URL path
		 * @param returnFormat
		 *            the return type expected for the API request
		 */
		private ApiType(Method method, ContentType contentType, boolean requiresObjectId, boolean withFullDetails,
				boolean requiresRelatedEntity, ApiReturnFormat returnFormat) {
			this.method = method;
			this.contentType = contentType;
			this.requiresObjectId = requiresObjectId;
			this.withFullDetails = withFullDetails;
			this.requiresRelatedEntity = requiresRelatedEntity;
			this.returnFormat = returnFormat;
		}

		private Method method;
		private ContentType contentType;
		private boolean requiresObjectId;
		private boolean withFullDetails;
		private boolean requiresRelatedEntity;
		private ApiReturnFormat returnFormat;

		// Getters
		public Method getMethod() {
			return method;
		}

		public ContentType getContentType() {
			return contentType;
		}

		public boolean isObjectIdNeeded() {
			return requiresObjectId;
		}

		public boolean isWithFullDetails() {
			return withFullDetails;
		}

		public boolean isWithRelatedEntity() {
			return requiresRelatedEntity;
		}

		public ApiReturnFormat getApiReturnFormat() {
			return returnFormat;
		}
	}

	/**
	 * ApiReturnFormat specifies if Mambu's returned JSON string represents a single object, a collection of objects or
	 * just a success/failure response
	 */
	public enum ApiReturnFormat {
		OBJECT, COLLECTION, BOOLEAN, RESPONSE_STRING
	}

	private ApiType apiType;

	// URL path details in the format: endPoint/objectID/relatedEntity
	private String endPoint;
	// The 'relatedEntity' part of the URL path
	private String relatedEntity;
	// API return format. Specified in the ApiType but can be modified
	private ApiReturnFormat returnFormat;

	private Class<?> entityClass;
	// The class of the object returned by Mambu
	private Class<?> returnClass;

	/**
	 * Constructor used with ApiType requests for which only one entity class needs to be specified, Example GET
	 * loans/123.
	 * 
	 * @param entityClass
	 *            determines API's endpoint (e.g. LoanAccount for loans/)
	 */
	public ApiDefinition(ApiType apiType, Class<?> entityClass) {
		initDefintion(apiType, entityClass, null);
	}

	/**
	 * Constructor used with ApiType requests for which two entity classes need to be specified, Example GET
	 * clients/123/loans. Currently this is used only with ApiType.GetOwnedEntities
	 * 
	 * @param entityClass
	 *            determines API's endpoint (e.g. LoanAccount for loans/)
	 * @param resultClass
	 *            determines the entity to be retrieved. E.g. loans in the API calls GET clients/333/loans
	 */

	public ApiDefinition(ApiType apiType, Class<?> entityClass, Class<?> resultClass) {
		initDefintion(apiType, entityClass, resultClass);
	}

	/**
	 * Initialise all API definition parameters for the specified ApiType
	 * 
	 * @param apiType
	 *            API type
	 * @param entityClass
	 *            entity class which identifies the api's end point
	 * @param resultClass
	 *            the class for the objects returned by the api. Needed for ApiType.GetOwnedEntities and is optional for
	 *            CREATE and UPDATE ApiTypes. For all other API types entity class determines also the result class
	 * 
	 */

	private void initDefintion(ApiType apiType, Class<?> entityClass, Class<?> resultClass) {

		if (apiType == null) {
			throw new IllegalArgumentException("apiType must not be null");
		}

		if (entityClass == null) {
			throw new IllegalArgumentException("entityClass must not be null");
		}
		this.apiType = apiType;
		this.entityClass = entityClass;
		relatedEntity = null;
		// Get defaults from the ApiType
		returnFormat = apiType.getApiReturnFormat();

		// Get the end point for the entityClass
		this.endPoint = getApiEndPoint(entityClass);

		switch (apiType) {
		case GET_ENTITY:
		case GET_ENTITY_DETAILS:
		case GET_LIST:
		case CREATE_FORM_ENTITY:
			returnClass = entityClass;
			break;
		case CREATE_JSON_ENTITY:
		case UPDATE_JSON:
			// If the result class was provided - use it. Otherwise assuming the return class is the same as the
			// entityClass. For example, when creating loans, LoanAccountExpanded is used as input and also as output.
			// But when creating a Document, JSONDocument is the input but the result class must be specified as
			// Document
			returnClass = (resultClass != null) ? resultClass : entityClass;
			break;
		case GET_OWNED_ENTITIES:
		case GET_RELATED_ENTITIES:
		case POST_OWNED_ENTITY:
		case PATCH_OWNED_ENTITY:
		case DELETE__OWNED_ENTITY:
			// For these API types the resultClass defines the 'relatedEntity' part. E.g. LOANS part in
			// /clients/1233/LOANS or transactions part: /loans/123/transactions. These types return the result class
			if (resultClass == null) {
				throw new IllegalArgumentException("resultClass must be not null for " + apiType.name());
			}
			relatedEntity = getApiEndPoint(resultClass);
			// These API types return object (or collection) of the resultClass (for OBJECT and COLLECTION return
			// formats)
			switch (returnFormat) {
			case OBJECT:
			case COLLECTION:
				returnClass = resultClass;
				break;
			case BOOLEAN:
				returnClass = Boolean.class;
				break;
			case RESPONSE_STRING:
				returnClass = String.class;
				break;
			}
			break;
		case DELETE_ENTITY:
			returnClass = Boolean.class;
			break;
		case POST_ENTITY_ACTION:
			// This type returns the entityClass class
			// For this API type the returned class is the same as the entityClass, E.g. LOANS part in
			// /clients/1233/LOANS or transactions part: /loans/123/transactions.
			if (resultClass == null) {
				throw new IllegalArgumentException("resultClass must be not null for " + apiType.name());
			}
			relatedEntity = getApiEndPoint(resultClass);
			returnClass = entityClass;
			break;
		}
	}

	// apiEndPointsMap maps Mambu classes to the corresponding Mambu API URL path endpoints.
	private final static Map<Class<?>, String> apiEndPointsMap;
	static {
		apiEndPointsMap = new HashMap<Class<?>, String>();

		apiEndPointsMap.put(Client.class, APIData.CLIENTS);
		apiEndPointsMap.put(ClientExpanded.class, APIData.CLIENTS);
		apiEndPointsMap.put(Group.class, APIData.GROUPS);
		apiEndPointsMap.put(GroupExpanded.class, APIData.GROUPS);

		apiEndPointsMap.put(LoanAccount.class, APIData.LOANS);
		apiEndPointsMap.put(LoanAccountExpanded.class, APIData.LOANS);
		apiEndPointsMap.put(LoanTransaction.class, APIData.TRANSACTIONS);
		apiEndPointsMap.put(Repayment.class, APIData.REPAYMENTS);

		apiEndPointsMap.put(SavingsAccount.class, APIData.SAVINGS);
		apiEndPointsMap.put(JSONSavingsAccount.class, APIData.SAVINGS);
		apiEndPointsMap.put(SavingsTransaction.class, APIData.TRANSACTIONS);

		apiEndPointsMap.put(Branch.class, APIData.BRANCHES);
		apiEndPointsMap.put(User.class, APIData.USERS);
		apiEndPointsMap.put(Centre.class, APIData.CENTRES);
		apiEndPointsMap.put(Currency.class, APIData.CURRENCIES);
		apiEndPointsMap.put(TransactionChannel.class, APIData.TRANSACTION_CHANNELS);

		apiEndPointsMap.put(Task.class, APIData.TASKS);
		apiEndPointsMap.put(JSONTask.class, APIData.TASKS);

		apiEndPointsMap.put(LoanProduct.class, APIData.LOANPRODUCTS);
		apiEndPointsMap.put(SavingsProduct.class, APIData.SAVINGSRODUCTS);

		apiEndPointsMap.put(Document.class, APIData.DOCUMENTS);
		apiEndPointsMap.put(JSONDocument.class, APIData.DOCUMENTS);

		apiEndPointsMap.put(CustomFieldSet.class, APIData.CUSTOM_FIELD_SETS);
		apiEndPointsMap.put(CustomField.class, APIData.CUSTOM_FIELDS);
		apiEndPointsMap.put(CustomFieldValue.class, APIData.CUSTOM_INFORMATION);

		apiEndPointsMap.put(GLAccount.class, APIData.GLACCOUNTS);
		apiEndPointsMap.put(GLJournalEntry.class, APIData.GLJOURNALENTRIES);
		apiEndPointsMap.put(Indicator.class, APIData.INDICATORS);

		apiEndPointsMap.put(CustomView.class, APIData.VIEWS);
		apiEndPointsMap.put(JSONActivity.class, APIData.ACTIVITIES);

		apiEndPointsMap.put(Image.class, APIData.IMAGES);

		apiEndPointsMap.put(SearchResult.class, APIData.SEARCH);

	}

	// Get an Api endpoint for a Mambu class
	private String getApiEndPoint(Class<?> entityClass) {

		if (entityClass == null) {
			throw new IllegalArgumentException("Entity Class cannot be NULL");
		}

		if (!apiEndPointsMap.containsKey(entityClass)) {
			throw new IllegalArgumentException("No Api end point is defined for class" + entityClass.getName());
		}
		return apiEndPointsMap.get(entityClass);
	}

	// Getters ////////////////
	public ApiType getApiType() {
		return apiType;
	}

	public String getEndPoint() {
		return endPoint;
	}

	public boolean isObjectIdNeeded() {
		return apiType.isObjectIdNeeded();
	}

	public Method getMethod() {
		return apiType.getMethod();
	}

	public ContentType getContentType() {
		return apiType.getContentType();
	}

	public String getRelatedEntity() {
		return relatedEntity;
	}

	public ApiReturnFormat getApiReturnFormat() {
		return returnFormat;
	}

	public boolean getWithFullDetails() {
		return apiType.isWithFullDetails();
	}

	public Class<?> getEntityClass() {
		return entityClass;
	}

	public Class<?> getReturnClass() {
		return returnClass;
	}

	// Setters for params which can be modified
	public void setApiType(ApiType apiType) {
		this.apiType = apiType;
	}

	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}

	public void setApiReturnFormat(ApiReturnFormat returnFormat) {
		this.returnFormat = returnFormat;
	}

}
