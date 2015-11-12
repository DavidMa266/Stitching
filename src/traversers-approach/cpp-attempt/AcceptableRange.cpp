class AcceptableRange
{
private:
	int lowerBound;
	int upperBound;
public:
	AcceptableRange(int lowerBound, int upperBound)
	{
		this->upperBound = upperBound;
		this->lowerBound = lowerBound;
	}

	int outOfRange(int value)
	{
		if(value<lowerBound || upperBound<value)
			return value;
		return 0;//0 for false
	}
}