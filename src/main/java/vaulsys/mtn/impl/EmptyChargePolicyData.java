package vaulsys.mtn.impl;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import vaulsys.mtn.ChargePolicyData;

@Entity
@DiscriminatorValue(value = "Empty")
public class EmptyChargePolicyData extends ChargePolicyData {

}
