

import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;


public class Runner extends Thread{

	WebDriver dr;
	Timer timer;
	boolean readyState;
	ArrayList<ClassRow> classList = null;
	long startTime;
	long timeInterval = 30000000000L;   //# of seconds * 1000000000 
	
	Runner(ArrayList<ClassRow> _classList){
		dr = new FirefoxDriver();
		classList = _classList;
		startTime = -1;
	}
	
	public void run(){
		//handle two kinds of timeout: 1. page isn't loaded after certain period of time; 2. error page is shown
		//skip the "add class to cart" procedure if the class is already in the cart
		
		
		//add class one at a time, based on priority
		
		/*
		 * Procedure:
		 * 0. log in to Student Center
		 * 1. go to "Add Class" tab, check the current cart, if the class is not in the cart, add it using its ID number
		 * 2. press "Enrolling step 2" to continue enrolling
		 * 3. when a class has been tried once, try to enroll the next unenrolled class
		 * 4. anytime when there is a timeout, restart the loop
		 * 
		 */
		
		try {
		
		dr.get("http://studentcenter.cornell.edu/");
		if (!pageIsReady(dr, "s Student Center")) {
			this.run();
			System.exit(0);
		}
		WebElement addClassPageLink = dr.findElement(By.id("DERIVED_SSS_SCL_LINK_ADD_ENRL"));
		addClassPageLink.click();
		System.out.println("Clicked ADD CLASS PAGE link");
		if (!pageIsReady(dr, "Put classes in your Shopping Cart and when you are satisfied with your class selections, proceed to step 2 of 3.")) {
			this.run();
			System.exit(0);
		}
		
		while (true) {   //use break to exit the loop
			
			boolean unenrolledClassExists = false;
			for (ClassRow curClass : classList) {
				if (!curClass.status.equals("Enrolled")) {
					unenrolledClassExists = true;
					break;
				}
			}
			if (!unenrolledClassExists) break;
				
			for (ClassRow curClass : classList) {
				System.out.println("Received "+curClass.deptName+" "+curClass.courseNum);
				if (!curClass.status.equals("Enrolled")) {
					if (!curClass.status.equals("")) {
					//	if (startTime == -1) startTime = System.nanoTime();
					//	else if (System.nanoTime()-startTime<timeInterval) {
						//	long waitTime = (timeInterval-(System.nanoTime()-startTime))/1000000;
							long waitTime = timeInterval/1000000;
							System.out.println("Wait for "+waitTime+"milliseconds");
							curClass.status = "Wait for "+(waitTime/1000)+"seconds";
							try {
								Thread.sleep(waitTime);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
					//		startTime = System.nanoTime();
					//	}
				//	} else {
				//		startTime = -1;
					}
					curClass.status = "Trying to enroll..";
					System.out.println("Trying to enroll "+curClass.deptName+" "+curClass.courseNum);
					
					Document doc = Jsoup.parse(dr.getPageSource());
					
					//check if current class is in the shopping cart
					Element classCart = doc.select("table.PSPAGECONTAINER table.PSPAGECONTAINER table.PSGROUPBOXNBO").get(0);
//					System.out.println(classCart.html());
					String[] tableString = curClass.getRunnerString();
					Boolean isContained = true;
					for (int i=0; i<4; i++) {
						System.out.println("tableString_"+i+" :"+tableString[i]);
						System.out.println(classCart.text());
						if (!classCart.text().contains(tableString[i])) {
							System.out.println("Cart doesn't contain the class. " + tableString[i]);
							isContained = false;
							break;
						}
//						System.out.println("Cart contains the class.");
					}
					if (isContained) System.out.println("Cart contains the class.");
					//handle the situation when current class is not in the shopping cart
					
					if (!isContained) {
						
						System.out.println(curClass.deptName+" "+curClass.courseNum+" is not in the cart");
						WebElement courseNumInput = dr.findElement(By.id("DERIVED_REGFRM1_CLASS_NBR$42$"));
						courseNumInput.sendKeys(curClass.sectionNums[0]);
						WebElement addClassButtonLink = dr.findElement(By.id("DERIVED_REGFRM1_SSR_PB_ADDTOLIST2$44$"));
						final String oldSource = dr.getPageSource();
						addClassButtonLink.click();
						System.out.println("clicked ADD CLASS link");
//						if (dr.getPageSource().contains("Select classes to add")) {
//							curClass.status = "Enrolled";
//							continue;
//						}
						
						(new WebDriverWait(dr, 6000)).until(new ExpectedCondition<Boolean>() {
							public Boolean apply(WebDriver dr) {
								if (dr.getPageSource().equals(oldSource)) return false;
								else return true;
							}
						});
						
						if (dr.getPageSource().contains("Duplicate - Already enrolled in Class Number")) {     //TODO: lack of server delay handling
							curClass.status = "Enrolled";
							continue;
						}
						
						//handle the situation when there are more than one sections of the class
						for (int i=0; i<3; i++) {
							System.out.println("sectionNums["+i+"] = "+curClass.sectionNums[i]);
						}
						if ((curClass.sectionNums[1].length()!=0)||(curClass.sectionNums[2].length()!=0)) {
							System.out.println("There is more than one section for this class");
							if (!pageIsReady(dr, "Related Class Sections")) {    //TODO: test ECON 3140 last lec last dis
								this.run();
								System.exit(0);
							}
							//doc = Jsoup.parse(dr.getPageSource());
							//Elements relatedClassTable = doc.select("table.PABACKGROUNDINVISIBLE");
						
							WebElement allSectionLink = null;
							if ((!(dr.getPageSource().contains(curClass.sectionNums[1])))) {
								allSectionLink = dr.findElement(By.linkText("View All Sections"));
								allSectionLink.click();
								System.out.println("Clicked ALL SECTION link");
							}
							if ((!(curClass.sectionNums[2].equals("")))&&(!(dr.getPageSource().contains(curClass.sectionNums[2])))) {
								allSectionLink = dr.findElement(By.linkText("View All Sections"));
								allSectionLink.click();
								System.out.println("Clicked ALL SECTION link");
							}
							
							
							if ((!pageIsReady(dr, curClass.sectionNums[1]))||(!pageIsReady(dr, curClass.sectionNums[2]))) {
								System.err.println("page doesn't contain all desired section numbers");
								this.run();
								System.exit(0);
							}
							
							
							
							if (!curClass.sectionNums[1].equals("")) {
								doc = Jsoup.parse(dr.getPageSource());
//								Elements elems = doc.select("body > form > table.PSPAGECONTAINER > tbody > tr > td > table.PSPAGECONTAINER > tbody > tr > td > table.PABACKGROUNDINVISIBLEWBO > tbody > tr > td > table.PABACKGROUNDINVISIBLE > tbody > tr > td > table.PSLEVEL1GRIDWBO > tbody > tr > td > table.PSLEVEL1GRID > tbody > tr");
//								Elements elems = doc.select("table.PSLEVEL1GRIDWBO");
//								Element firstSecTable = elems.get(0);
//								Elements firstSecList = firstSecTable.select("table.PSLEVEL1GRID tbody tr");
//								Elements elems = doc.select("td");
//								for (Element elem1 : elems) {
//									if (elem1.text().contains(curClass.sectionNums[1])) {
//										System.out.println(doc.select(""));
//									}
//								}
								//elem = elem.select("td").get(1);
//								elem = elem.select("table.PABACKGROUNDINVISIBLEWBO tbody tr td").get(0);
//								System.out.println(elem.html());
//								List<WebElement> firstSectionList = dr.findElements(By.cssSelector("body form table.PSPAGECONTAINER tbody tr td table.PSPAGECONTAINER tbody tr")).get(7).
//										findElements(By.cssSelector("td")).get(1).
//										findElements(By.cssSelector("table.PABACKGROUNDINVISIBLEWBO tbody tr td table.PABACKGROUNDINVISIBLE tbody tr")).get(3). //5
//										findElements(By.cssSelector("td")).get(1).findElements(By.cssSelector("table.PSLEVEL1GRIDWBO tbody tr")).get(1).findElements(By.cssSelector("td table.PSLEVEL1GRID tbody tr"));
								Elements elems = Jsoup.parse(dr.getPageSource()).select("table.PSLEVEL1GRIDWBO").get(0).select("table.PSLEVEL1GRID tbody");
								System.out.println(elems.html());
								List<WebElement> firstSectionList = dr.findElements(By.cssSelector("table.PSLEVEL1GRIDWBO")).get(0).findElements(By.cssSelector("table.PSLEVEL1GRID tbody tr"));
								for (WebElement curLine : firstSectionList) {
									if (curLine.getText().contains(curClass.sectionNums[1])) {
										System.out.println(curClass.sectionNums[1]+" is found in the page");
										
										WebElement curRadioButton = curLine.findElements(By.cssSelector("td input[type=radio]")).get(0);
										curRadioButton.click();
										System.out.println("Clicked RADIO BUTTON");
										break;
									}
								}
							}
							
							if (!curClass.sectionNums[2].equals("")) {
//								List<WebElement> secondSectionList = dr.findElements(By.cssSelector("body form table.PSPAGECONTAINER tbody tr td table.PSPAGECONTAINER tbody tr")).get(7).
//										findElements(By.cssSelector("td")).get(1).
//										findElements(By.cssSelector("table.PABACKGROUNDINVISIBLEWBO tbody tr td table.PABACKGROUNDINVISIBLE tbody tr")).get(5).
//										findElements(By.cssSelector("td")).get(1).findElements(By.cssSelector("table.PSLEVEL1GRIDWBO tbody tr")).get(1).findElements(By.cssSelector("td table.PSLEVEL1GRID tbody tr"));
								List<WebElement> secondSectionList = dr.findElements(By.cssSelector("table.PSLEVEL1GRIDWBO")).get(1).findElements(By.cssSelector("table.PSLEVEL1GRID tbody tr"));
								for (WebElement curLine : secondSectionList) {
									if (curLine.getText().contains(curClass.sectionNums[2])) {
										System.out.println(curClass.sectionNums[2]+" is found in the page");
										WebElement curRadioButton = curLine.findElements(By.cssSelector("td input[type=radio]")).get(0);
										curRadioButton.click();
										System.out.println("Clicked RADIO BUTTON");
										break;
									}
								}
							}
						
							WebElement nextLink = dr.findElement(By.id("DERIVED_CLS_DTL_NEXT_PB"));
							nextLink.click();
							System.out.println("Clicked NEXT link");
							if (!pageIsReady(dr, "Select classes to add - Enrollment Preferences")) {
								System.err.println("No response to the click of NEXT");
								this.run();
								System.exit(0);
							}
						
						//handle the situation when there is only one section for the class
						} else {
							System.out.println("Only one section for this class");
							if (!pageIsReady(dr, "Select classes to add - Enrollment Preferences")) {
								System.err.println("No response to the click of ENTER CLASS BY ID");
								this.run();
								System.exit(0);
							}
						}
						
						WebElement nextLink = dr.findElement(By.id("DERIVED_CLS_DTL_NEXT_PB"));
						nextLink.click();
						System.out.println("Clicked NEXT link");
						if (!pageIsReady(dr, "Put classes in your Shopping Cart and when you are satisfied with your class selections, proceed to step 2 of 3.")) {
							System.err.println("No response to the click of ADD CLASS TO CART");
							this.run();
							System.exit(0);
						}
						
						
					}
					}
					
				
					WebElement proceedLink = dr.findElement(By.id("DERIVED_REGFRM1_LINK_ADD_ENRL"));
					proceedLink.click();
					System.out.println("Clicked PROCEED link");
					if (!pageIsReady(dr, "Confirm classes")) {
						System.err.println("No response to the click of PROCEED TO STEP 2 OF 3");
						this.run();
						System.exit(0);
					}
					
					WebElement finishEnrollingLink = dr.findElement(By.id("DERIVED_REGFRM1_SSR_PB_SUBMIT"));
					finishEnrollingLink.click();
					System.out.println("Clicked FINISH ENROLLING link");
					if (!pageIsReady(dr, "View results")) {
						System.err.println("No response to the click of FINISH ENROLLING");
						this.run();
						System.exit(0);
					}
					
					if (dr.getPageSource().contains("You are already enrolled in this class")) {
						curClass.status = "Enrolled";
						System.out.println(curClass.status);
					} else {
						List<WebElement> classStatuses = dr.findElements(By.cssSelector("table.PSLEVEL1GRIDWBO tbody tr"));
						for (WebElement curLine : classStatuses) {
							if (curLine.getText().contains(curClass.deptName+" "+curClass.courseNum)) {
								WebElement statusPic = curLine.findElements(By.cssSelector("td")).get(2).findElement(By.cssSelector("div img"));
								System.out.println(statusPic.getAttribute("src"));
								if (statusPic.getAttribute("src").contains("/cs/cuselfservice/cache/PS_CS_STATUS_SUCCESS_ICN_1.gif")) {
									curClass.status = "Enrolled";
									System.out.println(curClass.status);
								} else {
									curClass.status = "Full class or time conflict";
									System.out.println(curClass.status);
								}
								break;
							}
						}
					}
					
					
					
					
//					int successPicIndex = dr.getPageSource().indexOf("/cs/cuselfservice/cache/PS_CS_STATUS_SUCCESS_ICN_1.gif");
//					successPicIndex = dr.getPageSource().substring(successPicIndex+55).indexOf("/cs/cuselfservice/cache/PS_CS_STATUS_SUCCESS_ICN_1.gif");
					
					
					
//					WebElement statusPic = dr.findElements(By.cssSelector("body form table.PSPAGECONTAINER tbody tr td table.PSPAGECONTAINER tbody tr")).get(9).
//							findElements(By.cssSelector("td")).get(1).findElements(By.cssSelector("table.PSGROUPBOX tbody tr")).get(3).
//							findElements(By.cssSelector("td")).get(1).findElements(By.cssSelector("table tbody tr")).get(1).
//							findElements(By.cssSelector("td")).get(2).findElement(By.cssSelector("div img"));
//					statusPic.getAttribute("src").contains("/cs/cuselfservice/cache/PS_CS_STATUS_SUCCESS_ICN_1.gif")
					
					
//					else if (successPicIndex!=-1) {
//						curClass.status = "Enrolled";
//						System.out.println(curClass.status);
//					} else {
//						curClass.status = "Full class or time conflict";
//						System.out.println(curClass.status);
//					}
					
					WebElement addPageLink = dr.findElement(By.linkText("Add"));
					addPageLink.click();
					System.out.println("Clicked ADD PAGE link");
					if (!pageIsReady(dr, "Select classes to add")) {
						System.err.println("No response to the click of ADD PAGE");
						this.run();
						System.exit(0);
					}
				}
		}
		System.out.println("All done!");
			
//		} catch (org.openqa.selenium.remote.UnreachableBrowserException e) {
//			try {
//				this.wait();
//			} catch (InterruptedException e1) {
//				e1.printStackTrace();
//			} catch (IllegalMonitorStateException e2) {
//				
//			}
		} catch (org.openqa.selenium.WebDriverException e) {
			if (e.getMessage().contains("Error communicating with the remote browser. It may have died.")) {
				//allow GUI to quit webdriver directly
			} else {
				e.printStackTrace();
				this.run();
			}
		}
	}
		
	
	
	
	public boolean pageIsReady(WebDriver driver, final String expectString) {
		readyState = false;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		(new WebDriverWait(driver, 6000)).until(new ExpectedCondition<Boolean>() {
					public Boolean apply(WebDriver dr) {
						if (dr.getPageSource().contains(expectString)) {
							readyState = true;
							return true;
						} else if (dr.getPageSource().contains("error")) {   //what does the error page look like? 
							readyState = false;
							return true;
						} else {
							return false;
						}
					}
		});
		return readyState;
	}
	

}
